<#-- 
  Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
        
        const statusPlace = 2;
        const orgPlace = 3;
        const table = document.getElementById("userList");
        const tr = table.getElementsByTagName("tr");

        function sortAll() {

            var organID = document.getElementById("selectOrg").value;
            var roleTypeId = document.getElementById("selectUserRole").value;
            var statusId = document.getElementById("selectStatus").value;
          
            for (i = 1; i < tr.length; i++) {
                var organization = tr[i].getElementsByTagName("td")[orgPlace].id;
                var role = tr[i].getElementsByTagName('li');
                var status = tr[i].getElementsByTagName("td")[statusPlace].id;
                var displayStatus = true;
                var displayOrg = true;
                var displayRole = false;

                if (organID != '-1' && organization != organID) {
                    displayOrg = false;
                }

                if (statusId != '-1' && status != statusId) {
                    displayStatus = false;
                }

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
                if (displayRole && displayOrg && displayStatus) {
                    tr[i].style.display = "";
                } else {
                    tr[i].style.display = "none";
                }
            }
        }

        function sortByUserRole() {
            var roleTypeId = document.getElementById("selectUserRole").value;

            for (i = 1; i < tr.length; i++) {
                var isDisplay = false;
                var role = tr[i].getElementsByTagName('li');

                if(roleTypeId == -1){
                    isDisplay = true;
                } else {
                    if(role != null){
                        for(j = 0; j<role.length ; j++){
                            if(isDisplay != true){
                                var userType = role[j].id;

                                if(userType == roleTypeId){
                                    isDisplay = true;
                                }
                            }
                        }
                    }
                }
                if (isDisplay) {
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
            <div class="row">
            <div class="col-sm-4">
                <#if user.isSuperUser() || user.isOrganizationAdmin() >
                    <select onchange="<#if user.isSuperUser()>sortAll();<#else>sortByUserRole();</#if>" id="selectUserRole" class="form-control">
                        <option value="-1">${i18nBundle.allRoles}</option>
                        <#list vipsLogicRoles as role>
                            <option value="${role.vipsLogicRoleId}">${i18nBundle["vipsLogicRole_" + role.vipsLogicRoleId]}</option>
                        </#list>
                    </select>
                </#if>
            </div>
            <div class="col-sm-4">
                <#if user.isSuperUser() >
                    <select onchange="sortAll();" id="selectOrg" class="form-control">
                        <option value="-1">${i18nBundle.allOrganizations}</option>
                        <#list organizations as org>
                            <option value=${org.organizationId}>${org.organizationName}</option>
                        </#list>  
                    </select>
                </#if>
            </div>
            <div class="col-sm-4">
                <#if user.isSuperUser() || user.isOrganizationAdmin()>
                    <select id="selectStatus" onchange="sortAll();" class="form-control">
                        <option value="-1">${i18nBundle.allStatuses}</option>
                        <option value="1">${i18nBundle["userStatus_" + 1]}</option>
                        <option value="2">${i18nBundle["userStatus_" + 2]}</option>
                        <option value="3">${i18nBundle["userStatus_" + 3]}</option>
                        <option value="4">${i18nBundle["userStatus_" + 4]}</option>
                        <option value="5">${i18nBundle["userStatus_" + 5]}</option>
                    </select>
                </#if>
            </div>
        </div>
            </td>
        </tr>
    <br>
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
                                <td id=${user.userStatusId}>${i18nBundle["userStatus_" + user.userStatusId]}</td>
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
