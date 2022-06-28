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
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.users}</title>
</#macro>
<#macro custom_css>
    <style type="text/css">
        ul {
        padding-inline-start: 1px;
        }
    </style>
    
</#macro>
<#macro custom_js>
    <script type="text/javascript">

        const orgPlace = 3;
        const table = document.getElementById("userList");
        const tr = table.getElementsByTagName("tr");

        function sortByOrgAndUserRole() {

            var organID = document.getElementById("selectOrg").value;
            var roleTypeId = document.getElementById("selectUserRole").value;
          
            for (i = 1; i < tr.length; i++) {
                var organization = tr[i].getElementsByTagName("td")[orgPlace].id;
                var role = tr[i].getElementsByTagName('li');
                var displayOrg = true;
                var displayRole = false;

                if (organID != '-1' && organization != organID) {
                    displayOrg = false;
                }
            
                var role = tr[i].getElementsByTagName('li');

                if(roleTypeId == -1){
                    displayRole = true;
                } else {
                    if(role != null){
                        for(j = 0; j<role.length ; j++){
                            if(displayRole != true){
                                var userType = role[j].id;

                                if(userType == roleTypeId){
                                    displayRole = true;
                                }
                            }
                        }
                    }
                }
                if (displayRole && displayOrg) {
                    tr[i].style.display = "";
                } else {
                    tr[i].style.display = "none";
                }
            }
        }

        function sortByUserRole() {
            var roleTypeId = document.getElementById("selectUserRole").value;

            for (i = 1; i < tr.length; i++) {
                var isDiplay = false;
                var role = tr[i].getElementsByTagName('li');

                if(roleTypeId == -1){
                    isDiplay = true;
                } else {
                    if(role != null){
                        for(j = 0; j<role.length ; j++){
                            if(isDiplay != true){
                                var userType = role[j].id;

                                if(userType == roleTypeId){
                                    isDiplay = true;
                                }
                            }
                        }
                    }
                }
                if (isDiplay) {
                    tr[i].style.display = "";
                } else {
                    tr[i].style.display = "none";
                }
            }
        }

    </script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.users}</h1>
        <p>
            <a href="/user?action=newUser" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
	</p>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>              
        <tr>
            <td>
                <#if user.isSuperUser() || user.isOrganizationAdmin() >
                    <select onchange="<#if user.isSuperUser()>sortByOrgAndUserRole();<#else>sortByUserRole();</#if>" id="selectUserRole" class="form-control">
                        <option value="-1">${i18nBundle.allRoles}</option>
                        <#list vipsLogicRoles as role>
                            <option value="${role.vipsLogicRoleId}">${i18nBundle["vipsLogicRole_" + role.vipsLogicRoleId]}</option>
                        </#list>
                    </select>
                </#if>
            <br>
                <#if user.isSuperUser() >
                    <select onclick="sortByOrgAndUserRole();" id="selectOrg" class="form-control">
                        <option value="-1">${i18nBundle.allOrganizations}</option>
                        <#list organizations as org>
                            <option value=${org.organizationId}>${org.organizationName}</option>
                        </#list>  
                    </select>
                </#if>
            </td>
        </tr>
        <div class="table-responsive">
            <table class="table table-striped" id="userList">
		<thead>
			<th>${i18nBundle.lastName}</th>
                        <th>${i18nBundle.firstName}</th>
			<th>${i18nBundle.status}</th>
                        <th>${i18nBundle.organizationId}</th>
                        <th>${i18nBundle.vipsLogicRoles}</th>
		</thead>
		<tbody>
                <!-- The ones that need treatment first -->
                <#assign userStatusOrdering = [2,1,3,4,5]>
                <#list userStatusOrdering as currentUserStatusId>
                    <#list users?sort_by("lastName") as user>
                        <#if user.userStatusId == currentUserStatusId>
                            <tr>
                                <td><a href="/user?action=viewUser&userId=${user.userId}">${user.lastName}</a></td>
                                <td><a href="/user?action=viewUser&userId=${user.userId}">${user.firstName}</a></td>
                                <td>${i18nBundle["userStatus_" + user.userStatusId]}</td>
                                <td id=${user.organizationId.organizationId}>${user.organizationId.organizationName}</td>
                                <td>
                                    <#list user.vipsLogicRoles as role>
                                        <ul>
                                            <li id=${role.vipsLogicRoleId} style="list-style-type:none">${i18nBundle["vipsLogicRole_" + role.vipsLogicRoleId]}</li>
                                        </ul>
                                    </#list>
                                </td>
                            </tr>
                        </#if>
                    </#list>
                </#list>
		</tbody>
            </table>
        </div>
</div>

</#macro>
<@page_html/>
