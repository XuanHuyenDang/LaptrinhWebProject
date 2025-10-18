package vn.flower.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthUtils {
  private AuthUtils() {}
  public static String currentUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return null;
    Object principal = auth.getPrincipal();
    if (principal instanceof UserDetails u) return u.getUsername();
    return String.valueOf(principal);
  }
}
