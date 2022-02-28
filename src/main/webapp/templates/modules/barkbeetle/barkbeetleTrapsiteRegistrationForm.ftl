<#--
    Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
  
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
	
--><#include "../../master.ftl">
<#assign siteName= seasonTrapsite.countyName + "/" + seasonTrapsite.municipalityName + "/" + seasonTrapsite.ownerName>
<#macro page_head>
	<title>Barkbilleovervåking: Registrer data for lokalitet ${siteName}, sesong ${seasonTrapsite.season}</title>
</#macro>
<#macro page_contents>
	<#if messageKey?has_content>
		<div class="alert alert-success">Data ble lagret ${.now}</div>
	</#if>
	<p>
		<a href="/barkbeetle?action=editSeasonTrapsite&season=${seasonTrapsite.season}&seasonTrapsiteId=${seasonTrapsite.seasonTrapsiteId}" class="btn btn-default back" role="button">${i18nBundle.back}</a>
	</p>
	<h1>Barkbilleovervåking</h1>
	<h2>Registrer data for lokalitet ${siteName}, sesong ${seasonTrapsite.season}</h2>
	<div class="row">
		<div class="col-md-11">
		<p>
		Påse at
		</p>
		<ul>
			<li>alle feller er av typen <strong>BEKA</strong> (=Nove, se bildet) og feromon av typen 
			<a href="http://kjemikonsult.no/Norsk/Produkter.html" target="new">Ipslure</a>
			(= Norlure; tilsendt fra Nibio).</li>
			<li>antall biller legges inn for rett tømmeperiode i rett ukenummer</li>
			<li>tallet legges inn på rett sted, som enten antall biller eller antall ml (volum målt med målebeger; 
			omregning til antall biller skjer automatisk).</li>
            <li>manglende data markeres med en "M" i "antall"-kolonnen for en felle. NB! Manglende data betyr at 
            data ikke skal regnes med, for eksempel grunnet en feil med fella eller at fella ikke har vært montert</li>
		</ul>
		<p>
		Se utdypende informasjon i 
		<a href="/images/modules/barkbeetle/Instruks_registranter_i_Barkbilleovervakingen.pdf" target="new">Instruks registranter</a>
		og <a href="/images/modules/barkbeetle/Montering_av_barkbillefelle.pdf">Monteringsanvisning
	Beka-felle</a>
		</p>
		</div>
		<div class="col-md-1">
		<img src="/images/modules/barkbeetle/Bilde_BEKA_felle.jpg" style="height: 150px;"/>
		</div>
	</div>
	<#if userIsAdmin || userIsCountyAdmin>
	<div class="alert alert-info">Registrant: <strong>${seasonTrapsite.userId.firstName} ${seasonTrapsite.userId.lastName}</strong>, tlf <strong>${seasonTrapsite.userId.phone!"Ikke oppgitt"}</strong>, e-post <strong>${seasonTrapsite.userId.email}</strong></div>
	</#if>
	<div class="singleBlockContainer">
		<div class="row">
		<form id="trapsiteRegistrationForm" action="/barkbeetle?action=trapsiteRegistrationFormSubmit" method="post" onsubmit="try{ return validateForm(this);}catch(e){console.info(e);return false;}">
		<input type="hidden" name="seasonTrapsiteId" value="${seasonTrapsite.seasonTrapsiteId}"/>
		<div class="table-responsive">
			<table class="table table-striped">
				<thead>
					<th></th>
					<th></th>
					<th colspan="2">BEKAFelle 1</th>
					<th colspan="2">BEKAFelle 2</th>
					<th colspan="2">BEKAFelle 3</th>
					<th colspan="2">BEKAFelle 4</th>
					<th></th>
					<th></th>
					<th></th>
					<#if userIsAdmin><th></th></#if>
				</thead>
				<thead>
					<th>Tømmeuke (periode)</th>
					<th>Tømmedato</th>
					<th>ml</th>
					<th>antall</th>
					<th>ml</th>
					<th>antall</th>
					<th>ml</th>
					<th>antall</th>
					<th>ml</th>
					<th>antall</th>
					<#if userIsAdmin><th>Status</th></#if>
				</thead>
				
				<tbody>
				<#list trapsiteRegistrations as reg>
				    <#assign tooEarly = reg.trapsiteRegistrationPK.week gt currentWeek && ! (currentWeek == 27 && reg.trapsiteRegistrationPK.week == 28)>
					<tr>
						<td rowspan="<#if seasonTrapsite.season gt 2021>3<#else>2</#if>">${reg.trapsiteRegistrationPK.week} (${reg?counter})</td>
						<td rowspan="2"><input class="form-control" type="date" name="${reg.trapsiteRegistrationPK.week}_dateRegistration" value="${reg.dateRegistration!""}"<#if tooEarly> disabled="disabled"<#else> required="required"</#if>/></td>
						<td><input class="form-control squeezemenot" type="number" name="${reg.trapsiteRegistrationPK.week}_trap1_ml" onblur="setCalculatedAmount(this);" value=""<#if tooEarly> disabled="disabled"</#if>/></td>
						<td><input class="form-control squeezemenot" type="text" onblur="validateRegField(this, true);" name="${reg.trapsiteRegistrationPK.week}_trap1" value="${reg.trap1!"M"}"<#if tooEarly> disabled="disabled"</#if>/></td>
						<td><input class="form-control squeezemenot" type="number" name="${reg.trapsiteRegistrationPK.week}_trap2_ml" onblur="setCalculatedAmount(this);" value=""<#if tooEarly> disabled="disabled"</#if>/></td>
						<td><input class="form-control squeezemenot" type="text" name="${reg.trapsiteRegistrationPK.week}_trap2" value="${reg.trap2!"M"}"<#if tooEarly> disabled="disabled"</#if>/></td>
						<td><input class="form-control squeezemenot" type="number" name="${reg.trapsiteRegistrationPK.week}_trap3_ml" onblur="setCalculatedAmount(this);" value=""<#if tooEarly || isExtended> disabled="disabled"</#if>/></td>
						<td><input class="form-control squeezemenot" type="text" name="${reg.trapsiteRegistrationPK.week}_trap3" value="${reg.trap3!"M"}"<#if tooEarly || isExtended> disabled="disabled"</#if>/></td>
						<td><input class="form-control squeezemenot" type="number" name="${reg.trapsiteRegistrationPK.week}_trap4_ml" onblur="setCalculatedAmount(this);" value=""<#if tooEarly || isExtended> disabled="disabled"</#if>/></td>
						<td><input class="form-control squeezemenot" type="text" name="${reg.trapsiteRegistrationPK.week}_trap4" value="${reg.trap4!"M"}"<#if tooEarly || isExtended> disabled="disabled"</#if>/></td>
						
						<td rowspan="2">
						<#if reg.dateRegistration?has_content>
							<#if userIsAdmin>
								<select class="form-control status_${reg.registrationStatusTypeId}" name="${reg.trapsiteRegistrationPK.week}_registrationStatusTypeId" onchange="this.className='form-control status_' + this.options[this.selectedIndex].value;">
								<#list registrationStatusTypes as registrationStatusType>
									<option class="status_${registrationStatusType.registrationStatusTypeId}" value="${registrationStatusType.registrationStatusTypeId}"
									<#if reg.registrationStatusTypeId?has_content && reg.registrationStatusTypeId==registrationStatusType.registrationStatusTypeId>selected="selected"</#if>
									>${registrationStatusType.title}</option>
								</#list>
								</select>
							</#if>
						</#if></td>
					</tr>
					<tr>
						<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap1Remarks" placeholder="Kommentar felle 1" value="${reg.trap1Remarks!""}"<#if tooEarly> disabled="disabled"</#if>/></td>
						<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap2Remarks" placeholder="Kommentar felle 2" value="${reg.trap2Remarks!""}"<#if tooEarly> disabled="disabled"</#if>/></td>
						<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap3Remarks" placeholder="Kommentar felle 3" value="${reg.trap3Remarks!""}"<#if tooEarly || isExtended> disabled="disabled"</#if>/></td>
						<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap4Remarks" placeholder="Kommentar felle 4" value="${reg.trap4Remarks!""}"<#if tooEarly || isExtended> disabled="disabled"</#if>/></td>
	
					</tr>
					<#if seasonTrapsite.season gt 2021>
					<tr>
						<td>Er det observert angrep av stor granbarkbille på stående gran i nærområdet/kommunen? Beskriv hva som er observert (omfang, sted m.m.; <a href="/images/modules/barkbeetle/Kjenn_igjen_angrep_stor_granbarkbille.pdf" target="new">instruksjon</a>)</td>
						<td colspan="8">
							<textarea class="form-control" name="${reg.trapsiteRegistrationPK.week}_observedAttacksDescription"<#if tooEarly> disabled="disabled"</#if>>${reg.observedAttacksDescription!""}</textarea>
						</td>
					</tr>
					</#if>
				</#list>
				</tbody>
			</table>
		</div>
		<#if seasonTrapsite.season lte 2021>
		<div class="form-group" style="padding-left: 5px; padding-right: 5px;">
			<label for="observedAttacksDescription">Er det observert angrep av stor granbarkbille på stående gran i nærområdet/kommunen?
		Beskriv hva som er observert (omfang, når, m.m.)</label>
			<textarea class="form-control" name="observedAttacksDescription">${seasonTrapsite.observedAttacksDescription!""}</textarea>
		</div>
		</#if>
		<div class="form-group" style="padding-left: 5px; padding-right: 5px; display: <#if currentWeek gte 33>block<#else>none</#if>;">
			<label for="maintenanceDescription">Registrer felledeler som må fornyes før neste sesong. Oppgi hva 
			slags felledel (oppsamlerkopp, låsepinne, oppsamlerkopp, jordspyd, kryss, trakt, midtrør, sil, hel 
			felle, annet) og antall. Husk å registrere leveringsadresse.</label>
			<textarea class="form-control" name="maintenanceDescription">${seasonTrapsite.maintenanceDescription!""}</textarea>
			<label for="countyName">Leveringsadresse for reservedeler</label>
			<input type="text" class="form-control" name="maintenanceAddress" placeholder="Leveringsadresse" value="${(seasonTrapsite.maintenanceAddress)!""}" />
		  </div>
		</div>
			<div class="form-group" style="padding-left: 5px;">
				  <div class="checkbox">
					  <label>
						<input type="checkbox" name="bekaConfirm" value="true"<#if !firstTimeReg> checked="checked"</#if> onclick="document.getElementById('submitButton').disabled=this.checked ? '':'disabled';"/>
						Jeg bekrefter at denne fellelokaliteten kun består av BEKAfeller med feromonet <a href="http://kjemikonsult.no/Norsk/Produkter.html" target="new">Ipslure</a>
					  </label>
				  </div>
			  </div>
			<div class="form-group" style="padding-left: 5px;">
				<button type="submit" id="submitButton" class="btn btn-default"<#if firstTimeReg> disabled="disabled"</#if>>Lagre</button>
			</div>
		</form>
		</div>
	</div>
</#macro>
<#macro custom_js>
<script type="text/javascript" src="/js/resourcebundle.js"></script>
<script src="//code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
<script type="text/javascript">
                // Make sure that hitting the ENTER key when inside a form input field doesn't 
                // accidentally submit the form. Also, that the ml to barkbeetle count is performed
                window.addEventListener('keydown',function(e){
					if(e.keyIdentifier=='U+000A'||e.keyIdentifier=='Enter'||e.keyCode==13)
					{
						if(e.target.nodeName=='INPUT'){
							if(e.target.name.indexOf("_ml") > 0)
							{
								setCalculatedAmount(e.target);
							}
							e.preventDefault();
							return false;
						}
					}
				},
				true
				);
		var theForm = document.getElementById("trapsiteRegistrationForm");
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		
		function setCalculatedAmount(sourceField)
		{

			var volumeValue = parseFloat(sourceField.value);
			if(!isNaN(volumeValue))
			{
				theForm[sourceField.name.substring(0,sourceField.name.indexOf("_ml"))].value = volumeValue * 50;
			}
		}
		
		function validateForm(theForm){
			var weeks = [21,24,28,33];
			for(var wi in weeks)
			{
				for(var i=1;i<=4;i++)
				{
					var regField = theForm[weeks[wi] + "_trap" + i];
					if(!validateRegField(regField,false))
					{
						alert("FEIL: Registreringsverdi for uke " + weeks[wi] + ", felle nr " + i + " må enten være et tall eller \"M\" for manglende data. ");
						return false;
					}
				}
				<#if seasonTrapsite.season gt 2021>
				if(weeks[wi] <= ${currentWeek} && theForm[weeks[wi] + "_observedAttacksDescription"].value.trim().length == 0)
				{
					alert("FEIL: Angrepsregistrering for alle utførte tømmerunder må være utfylt. Skriv eventuelt \"Ingen angrep registrert\".");
					return false;
				}
				</#if>
			}
			<#if currentWeek gte 33>
			// Check if maintenance description has content, issue warning and refuse submit if not
			if(theForm["maintenanceDescription"].value.trim() == "")
			{
				alert("Etter endt sesong skal behov for reservedeler oppgis, også hvis det ikke er behov for det (skriv i så fall f.eks. \"Ikke behov for reservedeler\".)");
				return false;
			}
			</#if>
			return true;
		
		}
		
		function validateRegField(regField, issueAlert)
		{
			if(isNaN(regField.value))
			{
				if(regField.value.trim() != "M")
				{
					if(issueAlert)
					{
						alert("FEIL: Registreringsverdi må enten være et tall eller \"M\" for manglende data.");
					}
					regField.classList.add("validateFalse");
					return false;
				}
			}
			regField.classList.remove("validateFalse");
			return true;
		}
		
</script>
</#macro>
<#macro custom_css>
	<link href="//code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<style type="text/css">
		select option[value="1"], select.status_1, span.status_1 {
			background-color: #ffe066 !important;
		}
		select option[value="2"], select.status_2, span.status_2 {
			background-color: #1aff88 !important;
		}
		select option[value="3"], select.status_3, span.status_3 {
			background-color: #ff6e66 !important;
		}

		input.validateFalse {
			border-color: red; 
		}
		input.squeezemenot {
			min-width: 70px;
		}	
	</style>
</#macro>
<@page_html/>