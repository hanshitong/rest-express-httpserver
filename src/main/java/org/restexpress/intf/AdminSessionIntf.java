package org.restexpress.intf;

import org.restexpress.Request;
import org.restexpress.domain.ex.AdminSessionInfo;

public interface AdminSessionIntf <T extends AdminSessionInfo> {
  T getAdminSessionInfo(Request request);
}
