<#include "common.ftl">

<@page>
  <div class="row">
  <div class="col-md-6">
  <h4 class="mb-3">Search</h4>
  <form class="" action="search" method="GET">
    <div class="form-group">
        <label for="q">Query</label>
        <input type="q" class="form-control" id="q" name="q" aria-describedby="Query">
    </div>
    <div class="form-group">
        <label for="using">Using</label>
        <select class="custom-select d-block w-100" id="using" required="" name="using">
              <option value="all">File name and Content</option>
              <option value="content">Content</option>
              <option value="filename">File name</option>
        </select>
    </div>
    <button type="submit" class="btn btn-primary">search</button>
  </form>
  </div>
  </div>
</@page>
