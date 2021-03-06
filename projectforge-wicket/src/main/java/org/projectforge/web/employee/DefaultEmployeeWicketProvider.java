/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.employee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.projectforge.business.fibu.EmployeeDO;
import org.projectforge.business.fibu.EmployeeStatus;
import org.projectforge.business.fibu.api.EmployeeService;
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext;
import org.projectforge.web.AbstractEmployeeWicketProvider;
import org.wicketstuff.select2.Response;

public class DefaultEmployeeWicketProvider extends AbstractEmployeeWicketProvider
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultEmployeeWicketProvider.class);

  private static final long serialVersionUID = 6228672123966093257L;

  private boolean withMyself;

  private List<EmployeeStatus> employeeStatusFilter;

  public DefaultEmployeeWicketProvider(EmployeeService employeeService, boolean withMyself, EmployeeStatus... employeeStatusFilter)
  {
    super(employeeService);
    this.withMyself = withMyself;
    this.employeeStatusFilter = Arrays.asList(employeeStatusFilter);
  }

  @Override
  public void query(String term, final int page, final Response<EmployeeDO> response)
  {
    boolean hasMore = false;
    Collection<EmployeeDO> result = new ArrayList<>();
    List<EmployeeDO> employeesWithoutLoginedUser = employeeService.findAllActive(false).stream()
        .filter(emp -> this.withMyself || emp.getUser().getPk().equals(ThreadLocalUserContext.getUserId()) == false)
        .filter(emp -> this.employeeStatusFilter.size() < 1 || (employeeService.getEmployeeStatus(emp) != null && this.employeeStatusFilter
            .contains(employeeService.getEmployeeStatus(emp))))
        .filter(emp -> emp.getUser().getEmail() != null && emp.getUser().getEmail().length() > 0)
        .collect(Collectors.toList());
    for (EmployeeDO emp : employeesWithoutLoginedUser) {
      if (StringUtils.isBlank(term) == false) {
        if (emp.getUser().getFullname().toLowerCase().contains(term.toLowerCase())) {
          result.add(emp);
        }
      } else {
        result.add(emp);
      }
      if (result.size() == pageSize) {
        hasMore = true;
        break;
      }
    }
    response.addAll(result);
    response.setHasMore(hasMore);
  }

}