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
	    .csrf(csrf -> csrf.disable()) // Tạm thời tắt CSRF để dễ test, nên bật lại sau
	    .authorizeHttpRequests(auth -> auth
	      // Public: trang chủ + tài nguyên tĩnh
	      .requestMatchers("/", "/index",
	          "/assets/**", "/css/**", "/js/**", "/images/**", "/slider/**").permitAll()

	      // === ĐÃ SẮP XẾP LẠI ĐỂ SỬA LỖI ===
	      // Public: Đặt quy tắc cụ thể nhất lên trước
	      .requestMatchers("/products/{id:[0-9]+}").permitAll() // Chi tiết sản phẩm (Cụ thể nhất)
	      .requestMatchers("/products").permitAll() // Danh sách sản phẩm
	      .requestMatchers("/products/**").permitAll() // Các URL khác dưới /products/ (Chung chung)
	      // ===================================

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
	      .loginProcessingUrl("/auth/login")
	      .usernameParameter("email")
	      .passwordParameter("password")
	      .successHandler(customSuccessHandler)
	      .failureUrl("/auth/login?error=true")
	      .permitAll()
	    )
	    .logout(logout -> logout
	      .logoutUrl("/auth/logout")
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