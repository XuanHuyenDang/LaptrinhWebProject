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
	    // .csrf(csrf -> csrf.disable()) // <-- ĐÃ XÓA DÒNG NÀY ĐỂ BẬT LẠI CSRF
	    /* // Tùy chọn: Nếu muốn dùng Cookie-based CSRF thay vì Session-based (mặc định)
	    .csrf(csrf -> csrf
	        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
	        // .csrfTokenRequestHandler(new CsrfTokenRequestAttributeNameRequestHandler()) // Nếu cần
	    )
	    */
	    .authorizeHttpRequests(auth -> auth
	      // Public: trang chủ + tài nguyên tĩnh
	      .requestMatchers("/", "/index",
	          "/assets/**", "/css/**", "/js/**", "/images/**", "/slider/**").permitAll()
	      // Public: sản phẩm
	      .requestMatchers("/products/{id:[0-9]+}").permitAll()
	      .requestMatchers("/products").permitAll()
	      .requestMatchers("/products/**").permitAll()
	      // Public: trang giới thiệu
	      .requestMatchers("/about/**", "/about").permitAll()
	      // Public: các trang xác thực
	      .requestMatchers("/auth/**", "/register", "/login").permitAll()
	      // Admin: yêu cầu quyền ADMIN
	      .requestMatchers("/admin/**").hasRole("ADMIN")
	      // Các request còn lại yêu cầu phải đăng nhập
	      .anyRequest().authenticated()
	    )
	    .formLogin(form -> form
	      .loginPage("/auth/login")
	      .loginProcessingUrl("/auth/login") // URL xử lý POST login
	      .usernameParameter("email")
	      .passwordParameter("password")
	      .successHandler(customSuccessHandler)
	      .failureUrl("/auth/login?error=true")
	      .permitAll()
	    )
	    .logout(logout -> logout
	      .logoutUrl("/auth/logout") // URL xử lý POST logout
	      .logoutSuccessUrl("/index")
	      .permitAll()
	    )
	    .sessionManagement(session -> session
	      .sessionCreationPolicy(
	        org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED
	      )
	    )
	    .authenticationProvider(authenticationProvider());

	  return http.build();
	}


	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}