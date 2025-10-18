package vn.flower.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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
	    .csrf(csrf -> csrf.disable())
	    .authorizeHttpRequests(auth -> auth
	      // Public: trang chủ + static
	      .requestMatchers("/", "/index",
	          "/assets/**", "/css/**", "/js/**", "/images/**", "/slider/**").permitAll()

	      // Public: TRANG SẢN PHẨM (page & api)
	      .requestMatchers(
	          "/products", "/products/**",      // danh sách / phân trang / lọc
	          "/product/**",                    // chi tiết sản phẩm kiểu /product/{id}
	          "/product-detail/**",             // nếu bạn dùng route này
	          "/api/products/**"                // API công khai cho trang sản phẩm (nếu có)
	      ).permitAll()
	      
	      //
	      .requestMatchers("/about/**", "/about").permitAll()
	      
	      // Public: auth pages
	      .requestMatchers("/auth/**", "/register", "/login").permitAll()

	      // Admin
	      .requestMatchers("/admin/**").hasRole("ADMIN")

	      // Các route còn lại yêu cầu đăng nhập
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
