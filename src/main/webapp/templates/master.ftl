<#-- 
    Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the NIBIO Open Source License as published by 
  NIBIO, either version 1 of the License, or (at your option) any
  later version.
  
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  NIBIO Open Source License for more details.
  
  You should have received a copy of the NIBIO Open Source License
  along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
-->
<#macro page_head>
        <title>master title</title>
</#macro>
<#macro custom_css>
	
</#macro>
<#macro custom_js>

</#macro>
<#macro page_contentwrap_start>
<!-- start contentwrapper-->
<div class="container">
	<nav class="navbar navbar-default" role="navigation">
	  <!-- Brand and toggle get grouped for better mobile display -->
	  <div class="navbar-header">
	    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
	      <span class="sr-only">Toggle navigation</span>
	      <span class="icon-bar"></span>
	      <span class="icon-bar"></span>
	      <span class="icon-bar"></span>
	    </button>
	    <a class="navbar-brand" href="/"><img src="/images/logo_vips.png" alt="VIPS logo"/> VIPSLogic</a>
	  </div>
	<!-- Collect the nav links, forms, and other content for toggling -->
	  <div class="collapse navbar-collapse navbar-ex1-collapse">
	    <ul class="nav navbar-nav">
	      <!--li class="active"><a href="#">Link</a></li-->
	      <!--li><a href="#">Link</a></li-->
	      <#if user.isOrganizationAdmin() || user.isSuperUser() >
	      <li class="active dropdown">
		<a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin<b class="caret"></b></a>
		<ul class="dropdown-menu">
		  <li><a href="/user">${i18nBundle.users}</a></li>
		  <li><a href="/forecastConfiguration">${i18nBundle.forecasts}</a></li>
		  <li><a href="/scheduling">${i18nBundle.scheduling}</a></li>
		  <li><a href="/weatherStation">${i18nBundle.weatherStations}</a></li>
		  <li><a href="/organism">${i18nBundle.organisms}</a></li>
		  <li><a href="/message">${i18nBundle.messages}</a></li>
		  <li><a href="/observation">${i18nBundle.observations}</a></li>
		</ul>
	      </li>
	      </#if>
	      <li><a href="/userguide/VIPSLogic_user_guide.html">${i18nBundle.help}</a></li>
	    </ul>
	    <form class="navbar-form navbar-right"  action="/logout" method="POST" role="logout">
	      <div class="form-group">
	      	<!--input type="hidden" name="logout" value="true"/-->
		${i18nBundle.loggedinas} ${user.firstName} ${user.lastName} (${user.organizationId.organizationName})
	      </div>
	      <button type="submit" class="btn btn-default">${i18nBundle.logout}</button>
	    </form>
	  </div><!-- /.navbar-collapse -->
	  
	</nav>

	<div class="row">
		<!-- Start contents container -->
		<div class="col-md-12">
	
	
</#macro>
<#macro page_contents>
        This is the default page contents.
</#macro>
<#macro page_contentwrap_end>
		</div><!-- End contents container -->
	</div><!-- End row with sidebar and contents container -->
	<hr>
	<footer>
		<div class="row">
			<div class="col-md-2">&copy; ${.now?string("yyyy")} <a href="http://www.nibio.no/" target="new">NIBIO</a></div>
			<div class="col-md-10"><p class="text-right"><small>[${i18nBundle.systemTime}: ${systemTime?datetime}]</small></p></div>
		</div>
	</footer>
</div>
<!-- end contentwrapper-->
</#macro>


<#macro page_html>
<!DOCTYPE html>
<html lang="no">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
	<link href="/css/3rdparty/bootstrap.min.css" rel="stylesheet" media="screen" />
	<!-- Custom styles for VIPSLogic -->
	<link href="/css/vipslogic.css" rel="stylesheet" media="screen" />
	<@custom_css/>
	<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!--[if lt IE 9]>
          <script src="/js/3rdparty/html5shiv.js"></script>
          <script src="/js/3rdparty/respond.min.js"></script>
        <![endif]-->
	<@page_head/>
</head>
<body>
	<@page_contentwrap_start/>
	<@page_contents/>
	<@page_contentwrap_end/>
	<!-- jQuery (necessary for Bootstraps JavaScript plugins) -->
	<script src="/js/3rdparty/jquery.min.js"></script>
	<!-- Include all compiled plugins (below), or include individual files as needed -->
	<script src="/js/3rdparty/bootstrap.min.js"></script>
	<@custom_js/>
</body>
</html>
</#macro> 
