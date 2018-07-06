package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.filter;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.ThreadLocalManager;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author liuyihan
 * @since 2018/6/22
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ThreadLocalFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        try {
            ThreadLocalManager.open();
            filterChain.doFilter(servletRequest, servletResponse);
        }finally {
            ThreadLocalManager.clear();
            ThreadLocalManager.close();
        }
    }

    @Override
    public void destroy() {

    }
}
