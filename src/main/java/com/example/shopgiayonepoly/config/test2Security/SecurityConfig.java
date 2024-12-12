package com.example.shopgiayonepoly.config.test2Security;

import com.example.shopgiayonepoly.config.CustomAccessDeniedHandler;
import com.example.shopgiayonepoly.entites.Staff;
import com.example.shopgiayonepoly.implement.StaffSecuritiImplement;
import com.example.shopgiayonepoly.repositores.StaffSecurityRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityConfig {

    @Autowired
    StaffSecurityRepository staffSecurityRepository;
    @Autowired
    StaffSecuritiImplement staffSecuritiImplement;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/staff/bill/vnpay-payment").permitAll()
                        .requestMatchers("/staff/bill/**").hasAnyRole("Quản trị viên", "Nhân viên bán hàng")
                        .requestMatchers("/staff/return-bill/**").hasAnyRole("Quản trị viên", "Nhân viên bán hàng")
                        .requestMatchers("/staff/transactionVNPay/**").hasAnyRole("Quản trị viên", "Nhân viên bán hàng")
                        .requestMatchers("/staff/shift/**").hasAnyRole("Quản trị viên")
                        .requestMatchers("/attribute/**").hasAnyRole("Quản trị viên")
                        .requestMatchers("/customer/**").hasAnyRole("Quản trị viên")
                        .requestMatchers("/staff/product/**").hasAnyRole("Quản trị viên")
                        .requestMatchers("/sale-product/**").hasAnyRole("Quản trị viên")
                        .requestMatchers("/staff-manage/**").hasAnyRole("Quản trị viên")
                        .requestMatchers("/voucher/**").hasAnyRole("Quản trị viên")
                        .requestMatchers("/login-api/**", "/ajax/**", "/css/**", "/fornText/**","/img/**", "/js/**", "/loading/**","/pagination/**", "/toast/**","/toastV2/**").permitAll()
                        .requestMatchers("/api-cashierInventory/**", "/api-timekeeping/**", "/shift-api/**", "/api-sale-product/**", "/api_voucher/**", "/transactionVNPay-api/**", "/staff-api/**", "/customer-api/**", "/send-email-bill", "/bill-api/**", "/register-api/**", "/register", "/login", "/logout", "/return-exchange-bill-api/**", "/sale/**", "/api-client/**").permitAll()
                        .requestMatchers("/onepoly/**").permitAll()
                        .requestMatchers("/profile/**","/forgotPassword/**").permitAll()
                        .requestMatchers("/staff/product/**").permitAll()
                        .requestMatchers("/product-api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                        .maximumSessions(1) // Giới hạn 1 session cho mỗi user
//                        .maxSessionsPreventsLogin(false)
                )
                .userDetailsService(staffSecuritiImplement)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home_manage", true)
                        .failureHandler(authenticationFailureHandler())  // Xử lý khi đăng nhập thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(new CustomAccessDeniedHandler())  // Xử lý khi quyền bị từ chối
                )
//                .exceptionHandling(exceptionHandling -> exceptionHandling
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            response.sendRedirect("/error?status=403"); // Chuyển hướng đến trang lỗi 403
//                        })
//                )
                .build();
    }

    // Xử lý khi đăng nhập thất bại
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                                AuthenticationException exception) throws IOException, ServletException {
                String username = request.getParameter("username");
                request.getSession().setAttribute("usernameFalse", username);

                // Kiểm tra tài khoản có tồn tại không
                Staff staff = staffSecurityRepository.findByAcountOrEmail(username, username);
                if (staff != null) {
                    // Tài khoản tồn tại nhưng mật khẩu không đúng
                    request.getSession().setAttribute("usernameError", null);
                    request.getSession().setAttribute("passwordError", "Mật khẩu không chính xác");
                } else {
                    // Tài khoản không tồn tại
                    request.getSession().setAttribute("usernameError", "Tài khoản không tồn tại");
                    request.getSession().setAttribute("passwordError", "Mật khẩu không chính xác");
                }

                // Chuyển hướng lại trang login với thông báo lỗi
                super.setDefaultFailureUrl("/login?error=true");
                super.onAuthenticationFailure(request, response, exception);
            }
        };
    }

    // Xử lý khi quyền bị từ chối
//    @Bean
//    public AccessDeniedHandler accessDeniedHandler() {
//        return new CustomAccessDeniedHandler();
//    }
}
