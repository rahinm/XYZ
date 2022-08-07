package net.dollmar.web.apicatalogue.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Part;

import spark.utils.IOUtils;

public class Utils {

  public static final String TOOLS_DIR="tools";
  public static final String DOC_GEN_SCRIPT_NAME = "api-doc-generator.sh";

  public static boolean isEmptyString(final String s) {
    return s == null || s.isEmpty();
  }

  public static <T extends Comparable<? super T>> List<T> sort(Collection<T> coll) {
    List<T> sorted = new ArrayList<>(coll);
    Collections.sort(sorted);

    return sorted;
  }

  public static String partToString(Part part) {
    try (InputStream inputStream = part.getInputStream()) {
      return IOUtils.toString(inputStream).trim();
    }
    catch (IOException e) {
      return "";
    }
  }	


  public static String buildVersionedFilename(String filename, String version) {
    if (isEmptyString(filename)) {
      return "";
    }
    if (isEmptyString(version)) {
      return filename;
    }
    int dotPosition = filename.lastIndexOf("."); 
    if (dotPosition != -1) {
      return filename.substring(0, dotPosition) + "-" + version + "." + filename.substring(dotPosition + 1);
    }
    else {
      return filename + "-" + version;
    }
  }

  public static String buildVersionedFilename(String apiName, String filename, String version) {

    if (isEmptyString(apiName)) {
      return buildVersionedFilename(filename, version);
    }
    String filenameExtension = "";
    int dotPosition = -1;

    if (!isEmptyString(filename)) {
      dotPosition = filename.lastIndexOf(".");
      filenameExtension = "." + filename.substring(dotPosition + 1);
    }
    if (isEmptyString(version)) {
      return apiName + filenameExtension;
    }
    else {
      return apiName + "-" + version + filenameExtension;
    }
  }


  public static String buildStyledHtmlPage(final String header, final String content) {
    StringBuilder sb = new StringBuilder();

    sb.append("<html><head>");
    sb.append("<script src='/Dollmar/js/sorttable.js' type='text/javascript'></script>");
    sb.append("<script src='/Dollmar/js/search.js' type='text/javascript'></script>");
    sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/Dollmar/StyleSheets/dollmar.css\"/>");
    sb.append("</head>");
    sb.append("<body bgcolor=\"#b8d6ca\">");
    sb.append(String.format("<h2>%s</h2>", header));
    sb.append(content);
    sb.append("</body>");
    sb.append("</html>");

    return sb.toString();
  }	

  public static class ProcessResult {
    public boolean success;
    public int exitCode;
    public String output;
  }

  public static ProcessResult generateApiDoc(String apiFileName, String docFileName) {
    String scriptName = String.format("%s/%s", TOOLS_DIR, DOC_GEN_SCRIPT_NAME);

    ProcessBuilder pb = new ProcessBuilder();
    pb.command(scriptName, apiFileName, docFileName);
    ProcessResult pr = new ProcessResult();

    try {
      Process process = pb.start();
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      StringBuilder sb = new StringBuilder();

      String line;
      while ((line = br.readLine()) != null) {
        //sb.append(line).append(System.getProperty("line.separator"));
        //sb.append(line).append("&#13;&#10");
        sb.append(line).append("<br>");
      }
      pr.exitCode = process.waitFor();
      pr.success = true;
      pr.output = sb.toString();
    }
    catch (Exception e) {
      pr.success = false;
      pr.exitCode = 1;
      pr.output = e.getMessage();
    }

    return pr;
  }

}
