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
<#macro custom_js>
    <script type="text/javascript">

	const table = document.getElementById("userList");
        const tr = table.getElementsByTagName("tr");

        function sortByOrg() {

          var organID = document.getElementById("selectOrg").value;
          
            for (i = 1; i < tr.length; i++) {
                var organisasjon = tr[i].getElementsByTagName("td")[3].id;
                var isDiplay = true;

                if (organID != '-1' && organisasjon != organID) {
                  isDiplay = false;
                }

                if (isDiplay) {
                  tr[i].style.display = "";
                } else {
                  tr[i].style.display = "none";
                }
            }
        }


        function sortByUserType() {
            var userTypeId = document.getElementById("selectUserType").value;

            for (i = 1; i < tr.length; i++) {
                var isDiplay = false;
                if(userTypeId == -1){
                    isDiplay = true;
                } else {
                    if(tr[i].getElementsByTagName("td")[4] == null){
                        isDiplay = false;

                    }else{
                        var counter = 4;
                        
                        while(tr[i].getElementsByTagName("td")[counter] && isDiplay != true){
                            var userType = tr[i].getElementsByTagName("td")[counter].id;

                            if(userType == userTypeId){
                                isDiplay = true;
                            }
                            counter++;
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

/*
          
            for (i = 1; i < tr.length; i++) {
                if(tr[i].getElementsByTagName("td")[4] == null){
                        isDisplay = false;
                        tr[i].style.display = "none";
                        console.log(tr[i].getElementsByTagName("td")[1].textContent);
                }else{
                    var counter = 4;
                    while(tr[i].getElementsByTagName("td")[counter]){

                        
                        var userType = tr[i].getElementsByTagName("td")[counter].id;
                        console.log(userType);
                        var isDiplay = true;

                        
                        if (userTypeId != '-1' && userType != userTypeId) {
                            isDiplay = false;
                        }

                        if (isDiplay) {
                          tr[i].style.display = "";
                        } else {
                          tr[i].style.display = "none";
                        }
                        
                        counter++;
                    }
                }
            }
        }
*/

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
                <select onclick="sortByUserType();" id="selectUserType">
                    <option value="-1">*Alle typer brukere</option>
                    <#list vipsLogicRoles as role>
                        <option value="${role.vipsLogicRoleId}">${i18nBundle["vipsLogicRole_" + role.vipsLogicRoleId]}</option>
                    </#list>
	  	</select>
            <br>
                <#if user.isSuperUser() >
                    <select onclick="sortByOrg();" id="selectOrg"  >
                        <option value="-1">*Alle organisasjoner</option>
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
                        <th>*Organisasjon</th>
                        <th>*type</th>
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
                            <#list user.vipsLogicRoles as i> 
                                <td id=${i.vipsLogicRoleId}>${i18nBundle["vipsLogicRole_" + i.vipsLogicRoleId]}</td>
                            </#list>
                            
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
