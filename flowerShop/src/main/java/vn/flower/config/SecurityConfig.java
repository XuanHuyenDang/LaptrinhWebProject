package vn.flower.Config;

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
	    // Bật CSRF (mặc định là bật, không cần gọi .csrf() nếu dùng cấu hình mặc định)
	    // Nếu cần tùy chỉnh (ví dụ dùng cookie):
	    // .csrf(csrf -> csrf
	    //    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
	    // )
	    .authorizeHttpRequests(auth -> auth
	      // Public: trang chủ, tài nguyên tĩnh, slider, hình ảnh sản phẩm
	      .requestMatchers("/", "/index",
	          "/assets/**", "/css/**", "/js/**", "/images/**", "/slider/**", "/uploads/**").permitAll() // Thêm /uploads/**
	      // Public: xem sản phẩm và danh sách sản phẩm
	      .requestMatchers("/products", "/products/{id:[0-9]+}").permitAll()
	      // Public: trang giới thiệu
	      .requestMatchers("/about").permitAll()
	      // Public: đăng ký, đăng nhập, xác thực OTP
	      .requestMatchers("/auth/**", "/register", "/login").permitAll()

	      // === CHO PHÉP VNPAY CALLBACK ===
	      .requestMatchers("/payment/vnpay-return", "/payment/vnpay-ipn").permitAll()
	      // ==============================

	      // API công khai (nếu có, ví dụ xem sản phẩm) - Hiện tại chưa có
	      // .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

	      // API cần đăng nhập (giỏ hàng, wishlist, chat)
	      .requestMatchers("/api/cart/**", "/api/wishlist/**", "/api/chat/**").authenticated() // Yêu cầu đăng nhập

	      // WebSocket endpoint
	      .requestMatchers("/ws/**").permitAll() // Cho phép kết nối WebSocket ban đầu

	      // Các trang cần đăng nhập (thông tin tài khoản, lịch sử đơn hàng, checkout, chat page)
	      .requestMatchers("/account/**", "/orders/**", "/checkout", "/cart", "/wishlist", "/chat").authenticated()
	      // TODO: Xem lại "/orders/return/**" cần đăng nhập không? -> Có, nên để trong authenticated()

	      // Admin: yêu cầu quyền ADMIN
	      .requestMatchers("/admin/**").hasRole("ADMIN")

	      // Các request còn lại yêu cầu phải đăng nhập
	      .anyRequest().authenticated()
	    )
	    .formLogin(form -> form
	      .loginPage("/auth/login")
	      .loginProcessingUrl("/auth/login") // URL xử lý POST login (Spring Security tự xử lý)
	      .usernameParameter("email")        // Tên input email trên form
	      .passwordParameter("password")     // Tên input password trên form
	      .successHandler(customSuccessHandler) // Xử lý chuyển hướng sau khi login thành công
	      .failureUrl("/auth/login?error=true") // URL khi login thất bại
	      .permitAll() // Cho phép mọi người truy cập trang login
	    )
	    .logout(logout -> logout
	      .logoutUrl("/auth/logout") // URL xử lý POST logout (Spring Security tự xử lý)
	      .logoutSuccessUrl("/?logout=true") // URL chuyển hướng sau khi logout thành công
	      .invalidateHttpSession(true) // Hủy session
	      .deleteCookies("JSESSIONID") // Xóa cookie session (nếu dùng session)
	      .permitAll() // Cho phép mọi người thực hiện logout
	    )
	    .sessionManagement(session -> session
	      .sessionCreationPolicy(
	        org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED // Chính sách quản lý session
	      )
	      // .maximumSessions(1).expiredUrl("/login?expired=true"); // Ví dụ: giới hạn 1 session / user
	    )
	    .authenticationProvider(authenticationProvider()); // Sử dụng provider đã cấu hình

	  return http.build();
	}


	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}