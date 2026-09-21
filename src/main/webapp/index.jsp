<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Welcome file: redirect into the MVC flow (index -> HomeServlet -> home.jsp) --%>
<% response.sendRedirect(request.getContextPath() + "/home"); %>