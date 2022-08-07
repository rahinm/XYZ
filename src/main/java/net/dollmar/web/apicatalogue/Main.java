package net.dollmar.web.apicatalogue;


import static spark.Spark.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Properties;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dollmar.web.apicatalogue.dao.ApiDefDao;
import net.dollmar.web.apicatalogue.filters.BasicAuthenticationFilter;
import net.dollmar.web.apicatalogue.filters.Filters;
import net.dollmar.web.apicatalogue.pages.ApiDefsViewPage;
import net.dollmar.web.apicatalogue.pages.ApiImportResultPage;
import net.dollmar.web.apicatalogue.utils.EntityBuilder;
import net.dollmar.web.apicatalogue.utils.Utils;
import net.dollmar.web.apicatalogue.utils.Utils.ProcessResult;
import spark.utils.IOUtils;



public class Main {

  public static final int DEFAULT_PORT = 10080;
  public static final String SVC_NAME = "APICatalogue";
  public static final String SVC_VERSION = "v1";

  public static final String HOME_PATH = String.format("/%s/%s", Main.SVC_NAME, "home");
  public static final String APIS_PATH = String.format("/%s/%s", Main.SVC_NAME, "apis");
  public static final String IMPORT_PATH = String.format("/%s/%s", Main.SVC_NAME, "import");
  public static final String DOCS_PATH = String.format("/%s/%s", Main.SVC_NAME, "docs");
  public static final String DOWNLOAD_PATH = String.format("/%s/%s", Main.SVC_NAME, "downloads");
  

  private static final String API_DIR = "data/apis";
  private static final String CFG_FILE = "config/APICatalogue.properties";

  private static Logger logger = LoggerFactory.getLogger(Main.class);


  private static void configure() {
    File configDir = new File(CFG_FILE).getParentFile();
    if (!configDir.exists()) {
      configDir.mkdirs();
    }		
    Properties props = new Properties();
    try (InputStream input = new FileInputStream(CFG_FILE)) {
      props.load(input);
      for (String p : props.stringPropertyNames()) {
        System.setProperty(p, props.getProperty(p));
      }
    }
    catch (IOException e) {
      System.err.println(String.format("WARN: Failed to load configuration data [Reason: %s]", e.getMessage()));
    }		
  }

  public static void main(String[] args) {
    configure();
    String hostName = "localhost";
    try {
      hostName = InetAddress.getLocalHost().getCanonicalHostName();
    }
    catch (UnknownHostException e) {
      // DO nothing
    }
    String scheme = "http";
    int serverPort = Integer.getInteger("apicatalogue.listener.port", DEFAULT_PORT);
    port(serverPort);
    if (Boolean.getBoolean("apicatalogue.network.security")) {
      // disable insecure algorithms
      Security.setProperty("jdk.tls.disabledAlgorithms",
          "SSLv3, TLSv1, TLSv1.1, RC4, MD5withRSA, DH keySize < 1024, EC keySize < 224, DES40_CBC, RC4_40, 3DES_EDE_CBC");

      String keyStoreName = System.getProperty("apicatalogue.keystore.filename");
      String keyStorePassword = System.getProperty("apicatalogue.keystore.password");

      if (Utils.isEmptyString(keyStoreName) || Utils.isEmptyString(keyStorePassword)) {
        System.err.println("ERROR: Keystore name or password is not set.");
        return;
      }
      scheme = "https";
      secure(keyStoreName, keyStorePassword, null, null);
    }

    String baseUrl = String.format("%s://%s:%d/%s/", scheme, hostName, serverPort, SVC_NAME);

    // root location for static pages (e.g. API Docs)
    staticFiles.location("/static");

    logger.info(String.format("Starting server on port: %d", serverPort ));
    logger.info("Application available at ==> " + baseUrl); 

    // routes

    before(new BasicAuthenticationFilter("*"));


    get(APIS_PATH, (req, resp) -> {
      resp.status(200);
      return new ApiDefsViewPage().render(req.queryMap().toMap());
    });

    get(APIS_PATH + "/:apiFilename", (req, resp) -> {
      String apiFilename = req.params(":apiFilename");
      File apiFile = new File(API_DIR, apiFilename);
      if (!apiFile.exists()) {
        resp.status(404);
        return String.format("Error: API file '%s' does not exist", apiFilename);  
      }
      String content = new String(Files.readAllBytes(Paths.get(API_DIR, apiFilename)));
      resp.type("application/text");
      resp.status(200);

      return content;
    });   


    get(DOCS_PATH, (req, resp) -> {
      String apiName = req.queryParams("name");
      String apiVersion = req.queryParams("version");
      File apiFile = new File(new File(API_DIR, apiName + "-" + apiVersion), apiName + "-" + apiVersion + ".html");
      if (!apiFile.exists()) {
        resp.status(404);
        return String.format("Error: Documentation for API '%s:%s' does not exist", apiName, apiVersion);  
      }
      String content = new String(Files.readAllBytes(Paths.get(apiFile.toURI())));
      resp.type("text/html");
      resp.status(200);

      return content;
    });   
    

    get(DOWNLOAD_PATH, (req, resp) -> {
      String apiName = req.queryParams("name");
      String apiVersion = req.queryParams("version");
      String apiFilename = apiName + "-" + apiVersion + ".zip";
      File apiFile = new File(new File(API_DIR, apiName + "-" + apiVersion), apiFilename);

      if (!apiFile.exists()) {
        resp.status(404);
        return String.format("Error: API definition file '%s:%s' does not exist", apiName, apiVersion);  
      }
      
      resp.raw().setContentType("application/zip");
      resp.raw().setHeader("Content-Disposition", "attachment; filename=" + apiFilename);
      try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(apiFile))) {
        try(BufferedOutputStream bos = new BufferedOutputStream(resp.raw().getOutputStream())) {
          byte[] buf = new byte[8192];
          int length;
          while ((length = bis.read(buf)) > 0) {
            bos.write(buf, 0, length);
          }
          bos.flush();
        }
      }
      String content = new String(Files.readAllBytes(Paths.get(apiFile.toURI())));
      resp.type("text/html");
      resp.status(200);

      return content;
    });   


    post(IMPORT_PATH, (req, resp) -> {
      req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(API_DIR));

      Part apiNamePart = req.raw().getPart("api_name");
      Part apiDescPart = req.raw().getPart("api_description");
      Part apiVersionPart = req.raw().getPart("api_version");
      Part apiTypePart = req.raw().getPart("api_type");
      Part apiUsagePart = req.raw().getPart("api_usage");
      Part apiStatusPart = req.raw().getPart("api_status");
      Part apiFilePart = req.raw().getPart("api_file");

      String apiName = Utils.partToString(apiNamePart);
      String apiDesc = Utils.partToString(apiDescPart);
      String apiVersion = Utils.partToString(apiVersionPart);
      String apiType = Utils.partToString(apiTypePart);
      String apiUsage = Utils.partToString(apiUsagePart);
      String apiStatus = Utils.partToString(apiStatusPart);

      if (Utils.isEmptyString(apiName) 
          || Utils.isEmptyString(apiVersion) 
          || Utils.isEmptyString(apiType) 
          || Utils.isEmptyString(apiFilePart.getSubmittedFileName())) {
        resp.status(400);

        return Utils.buildStyledHtmlPage("Error", "Missing input parameter");
      }
      File saveDir = new File(API_DIR, apiName + "-" + apiVersion);
      if (!saveDir.exists()) {
        saveDir.mkdirs();
      }
      File importFile = new File(saveDir, Utils.buildVersionedFilename(apiName, apiFilePart.getSubmittedFileName(), apiVersion));
      try (InputStream inputStream = apiFilePart.getInputStream()) {
        OutputStream outputStream = new FileOutputStream(importFile);
        IOUtils.copy(inputStream, outputStream);
        outputStream.close();
      }

      // persist a new API definition item in the database
      ApiDefDao dao = new ApiDefDao();

      if (dao.getApiDef(apiName, apiVersion) != null) {
        return String.format("Error: API defition for '%s:%s' has already been imported", apiName, apiVersion);
      }

      ProcessResult pr = Utils.generateApiDoc(importFile.getPath(), String.format("%s-%s.html", apiName, apiVersion));
      if (pr.success && pr.exitCode == 0) {
        dao.saveApiDef(EntityBuilder.buildApiDef(apiName, apiDesc, apiVersion, apiType, apiUsage, apiStatus, importFile.getName()));
      }
      return new ApiImportResultPage().buildHtmlForImportResult(pr);
      //return Utils.buildStyledHtmlPage("Error", "Failed to import API definition." + "<br>" + pr.output);
    });

    //Set up after-filters (called after each get/post)
    after("*", Filters.addGzipHeader);		
  }
}
