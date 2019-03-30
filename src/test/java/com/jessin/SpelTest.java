package com.jessin;

import com.jessin.practice.bean.User;
import com.jessin.practice.service.USerService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author zexin.guo
 * @create 2019-02-26 上午9:52
 **/
public class SpelTest {

    private ExpressionParser parser = new SpelExpressionParser();

    private LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Test
    public void test() {
        User user = new User();
        user.setId(10000);
        try {
            Method initMethod = USerService.class.getDeclaredMethod("updateAccount", User.class);
            String[] paramNames = discoverer.getParameterNames(initMethod);
            System.out.println(Arrays.asList(paramNames));
            Integer id = parseSpel(initMethod, new Object[]{user}, "#user.getId()", Integer.class, 0);
            System.out.println(id);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T parseSpel(Method method, Object[] arguments, String spel, Class<T> clazz, T defaultResult) {
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            // 方法参数名和对应的参数值，例如UserService#updateAccount(User user)
            // 则填入user -> 对应user实例
            context.setVariable(params[len], arguments[len]);
        }

        try {
            Expression expression = parser.parseExpression(spel);
            return expression.getValue(context, clazz);
        } catch (Exception e) {
            return defaultResult;
        }
    }

    @Test
    public void testParserContext() {
        ExpressionParser parser = new SpelExpressionParser();
        ParserContext parserContext = new ParserContext() {
            @Override
            public boolean isTemplate() {
                return true;
            }

            @Override
            public String getExpressionPrefix() {
                return "#{";
            }

            @Override
            public String getExpressionSuffix() {
                return "}";
            }
        };
        String template = "'Hello ''World!'";
        Expression expression = parser.parseExpression(template, parserContext);
        Assert.assertEquals("Hello World!", expression.getValue());

    }
}