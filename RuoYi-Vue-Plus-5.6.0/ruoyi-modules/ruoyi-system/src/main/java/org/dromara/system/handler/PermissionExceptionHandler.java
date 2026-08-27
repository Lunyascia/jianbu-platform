package org.dromara.system.handler;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.system.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 权限码异常处理（提示缺少的具体功能中文名）
 */
@Slf4j
@RestControllerAdvice
public class PermissionExceptionHandler {

    @Autowired
    private SysMenuMapper menuMapper;

    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限码校验失败'{}'", requestURI, e.getMessage());
        String permission = e.getPermission();
        String name = menuMapper.selectMenuNameByPerms(permission);
        String display = StrUtil.isNotBlank(name) ? name : (StrUtil.isBlank(permission) ? "未知" : permission);
        String msg = "缺少功能权限：" + display + "，请联系系统管理员在权限配置中授予该功能";
        return R.fail(HttpStatus.HTTP_FORBIDDEN, msg);
    }
}
