package net.dollmar.web.apicatalogue.pages;


import net.dollmar.web.apicatalogue.utils.Utils;
import net.dollmar.web.apicatalogue.utils.Utils.ProcessResult;


public class ApiImportResultPage {

  
  public String buildHtmlForImportResult(final ProcessResult pr) {
    assert(pr != null);
    String finalResult;
    String message;
    if (pr.success && pr.exitCode == 0) {
      finalResult = "Success";
      message = "API definition successfully uploaded and imported into database";
    }
    else {
      finalResult = "Failed";
      message = "Failed to import API definition";
    }
    String title = "API import result";
    StringBuilder sb = new StringBuilder();
    sb.append("<div style='display:block'>");
    sb.append("<form action='/APICatalogue/upload.html' method='GET'>");
    sb.append("<table>");
    sb.append("<tr><td>Import Status:</td> <td><input type='text' id='status' size='40' value='" + finalResult + "' readonly></td></tr>");
    sb.append("<tr><td>Import Result:</td> <td><input type='text' id='result' size='40' value='" + message + "' readonly></td></tr>");
    sb.append("<tr><td>Import Log:</td> <td><textaread id='import_log' rows='6' cols='40'>" + pr.output + "</textarea></td></tr>");
    sb.append("</table>");
    sb.append("<br>");
    sb.append("<input type='submit' value='Import Another'>");
    sb.append("</form>");
    
    return Utils.buildStyledHtmlPage(title, sb.toString());
  }
}
