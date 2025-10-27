package vn.flower.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// Import thêm CsrfTokenRepository nếu muốn tùy chỉnh (ví dụ: CookieCsrfTokenRepository)
// import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	@Autowired
	private CustomSuccessHandler customSuccessHandler;

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(customUserDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	  http
	    // Bật CSRF (mặc định là bật)
	    // .csrf(csrf -> csrf...) // Tùy chỉnh nếu cần
	    .authorizeHttpRequests(auth -> auth
	      // Public paths
	      .requestMatchers("/", "/index",
	          "/assets/**", "/css/**", "/js/**", "/images/**", "/slider/**", "/uploads/**").permitAll() // Cho phép truy cập tài nguyên tĩnh và uploads
	      .requestMatchers("/products", "/products/{id:[0-9]+}").permitAll() // Xem sản phẩm
	      .requestMatchers("/about").permitAll() // Trang giới thiệu
	      .requestMatchers("/auth/**", "/register", "/login").permitAll() // Đăng ký/Đăng nhập
	      .requestMatchers("/payment/vnpay-return", "/payment/vnpay-ipn").permitAll() // VNPAY callbacks
	      .requestMatchers("/ws/**").permitAll() // WebSocket endpoint

	      // Authenticated APIs
	      .requestMatchers("/api/cart/**", "/api/wishlist/**", "/api/chat/**").authenticated()

	      // Authenticated Pages
	      .requestMatchers("/account/**", "/orders/**", "/checkout", "/cart", "/wishlist", "/chat").authenticated()
	      // Trang yêu cầu trả hàng cũng cần đăng nhập
	      .requestMatchers("/orders/return/**").authenticated()

	      // Admin Area - Yêu cầu quyền ADMIN cho tất cả các đường dẫn /admin/**
          // Rule này đã bao gồm cả /admin/discounts/**
	      .requestMatchers("/admin/**").hasRole("ADMIN")

	      // All other requests require authentication
	      .anyRequest().authenticated()
	    )
	    .formLogin(form -> form
	      .loginPage("/auth/login")
	      .loginProcessingUrl("/auth/login")
	      .usernameParameter("email")
	      .passwordParameter("password")
	      .successHandler(customSuccessHandler)
	      .failureUrl("/auth/login?error=true")
	      .permitAll()
	    )
	    .logout(logout -> logout
	      .logoutUrl("/auth/logout")
	      .logoutSuccessUrl("/?logout=true")
	      .invalidateHttpSession(true)
	      .deleteCookies("JSESSIONID")
	      .permitAll()
	    )
	    .sessionManagement(session -> session
	      .sessionCreationPolicy(
	        org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED
	      )
	      // .maximumSessions(1).expiredUrl("/login?expired=true");
	    )
	    .authenticationProvider(authenticationProvider()); // Sử dụng provider đã cấu hình

	  return http.build();
	}


	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}