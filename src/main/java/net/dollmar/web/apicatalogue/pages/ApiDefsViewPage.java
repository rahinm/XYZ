package net.dollmar.web.apicatalogue.pages;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.dollmar.web.apicatalogue.Main;
import net.dollmar.web.apicatalogue.dao.ApiDefDao;
import net.dollmar.web.apicatalogue.entity.ApiDef;
import net.dollmar.web.apicatalogue.utils.Utils;

public class ApiDefsViewPage {


	public String render(Map<String, String[]> qm) {
	  ApiDefDao apiDao = new ApiDefDao();

		return buildHtmlTableForApiDefs("List of registered API definitions", apiDao.getAllApiDefs());
	}


	@SuppressWarnings("unchecked")
	private String buildHtmlTableForApiDefs(final String title, final Collection<ApiDef> apps) {

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<h3>Number of registered API definitions = %d</h3>", apps.size()));

		sb.append("<input type='text' id='apiName' name='search_input' onkeyup='searchApiDef()' placeholder='Search for API defnitions ...'>");

		sb.append("<table id ='apisTable' class='sortable' border='1'>");
		sb.append("<thead><tr><th>Id</th>");
		sb.append("<th>API Name</th>");
		sb.append("<th>API Description</th>");
		sb.append("<th>Version</th>");
		sb.append("<th>Type</th>");
		sb.append("<th>Usage</th>");
		sb.append("<th>Status</th>");
		sb.append("<th>Document</th>");
		sb.append("<th>Download</th>");
		sb.append("<th>Remove</th>");
		sb.append("</tr></thead>");

    if (apps != null) {
      // sort the collection for better presentation
      List<ApiDef> apiList = Utils.sort(apps);
      sb.append("<tbody>");
      for (ApiDef api : apiList) {
        String aid = "" + api.getId();
        sb.append("<tr><td>").append(aid).append("</td>");
        sb.append("<td>").append(api.getApiName()).append("</td>");
        sb.append("<td>").append(api.getApiDesc()).append("</td>");
        sb.append("<td>").append(api.getVersion()).append("</td>");
        sb.append("<td>").append(api.getType()).append("</td>");
        sb.append("<td>").append(api.getUsage()).append("</td>");
        sb.append("<td>").append(api.getStatus()).append("</td>");
        sb.append("<td>").append(String.format("<a href='/%s/docs?name=%s&version=%s' target='ifrm'>API Doc", Main.SVC_NAME, api.getApiName(), api.getVersion())).append("</td>");
        sb.append("<td>").append(String.format("<a href='/%s/downloads?name=%s&version=%s' target='ifrm'>Download", Main.SVC_NAME, api.getApiName(), api.getVersion())).append("</td>");
        sb.append("<td>").append("Delete").append("</td>");
        sb.append("</tr>");
			}
      sb.append("</tbody>");
		}

		sb.append("</table>");
		sb.append("<br>");

		return Utils.buildStyledHtmlPage(title, sb.toString());
	}


//	private String createLinkForPopup(String rowId) {
//		String url = String.format("%s?appId=%s", Main.EDIT_PATH, rowId);
//
//		StringBuilder sb = new StringBuilder();
//		sb.append(String.format("<a href=\"%s\" ", url)).append("target=\"popup\" ");
//		sb.append(
//				String.format("onclick=\"window.open('%s', 'popup', 'width=1000, height=600'); return false;\">", url));
//		sb.append(rowId).append("</a>");
//		return sb.toString();
//	}

}
