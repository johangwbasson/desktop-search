<#include "common.ftl">
<#setting url_escaping_charset='UTF-8'>
<@page>
  <table>
    <tr>
      <td colspan="2">Hits: ${result.hits}</td>
    </tr>
    <tr>
      <td class="divide"></td>
    </tr>
    <#list result.results as res>
    <tr>
      <td>
        <#if res.extension == "">
        <span class="fiv-cla fiv-icon-blank"></span>
        <#else>
        <span class="fiv-cla fiv-icon-${res.extension}"></span>
        </#if>
      </td>
      <td class="filename"><a href="/get?f=${res.fullPath?url}">${res.filename}</a></h5></td>
    </tr>
    <tr>
      <td class="directory" colspan="2">${res.fullPath}</td>
    </tr>
    <tr>
      <td colspan="2">
        <table width="100%">
          <#list res.frags as frag>
          <tr class="frag">
            <td>${frag}</td>
          </tr>
          </#list>
          </table>
      </td>
    </tr>
    <tr>
      <td class="divide"></td>
    </tr>
    </#list>
  </table>



   <#--  <table class="table">
     <thead class="thead-dark">
       <tr>
         <th scope="col"></th>
         <th scope="col">Score</th>
         <th scope="col">Filename</th>
         <th scope="col">Directory</th>
         <th scope="col">Size</th>
         <th scope="col">Type</th>
         <th scope="col">Actions</th>
       </tr>
     </thead>
     <tbody>
        <#list results as res>
       <tr>
         <th scope="row"><span class="fiv-cla fiv-icon-${res.extension}"></span></th>
         <td><a href="/get?f=${res.fullPath?url}">${res.filename}</a></td>
         <td>${res.directory}</td>
         <td>${res.size}</td>
         <td> ${res.contentType}</td>
         <td></td>
       </tr>
       </#list>
       </tbody>
      </table>  -->
</@page>