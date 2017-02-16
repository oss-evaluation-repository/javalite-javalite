package org.javalite.activeweb.controller_filters;

import org.javalite.activeweb.RequestSpec;
import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * @author igor on 2/8/17.
 */
public class JSONLogSpec extends RequestSpec {


    @Before
    public void before(){
        SystemStreamUtil.replaceOut();
        System.setProperty("activeweb.log.request", "true");
    }

    @After
    public void after(){
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldPrintJSONLog() throws IOException, ServletException {

        request.setServletPath("/logging");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        a(response.getContentAsString()).shouldBeEqual("ok");
        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        Map log0 = JsonHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        Map message = (Map) log0.get("message");
        the(message.get("info")).shouldBeEqual("executing controller");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("index");
        the(message.get("method")).shouldBeEqual("GET");


        Map log1 = JsonHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("INFO");
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        message = (Map) log1.get("message");
        the(message.get("success")).shouldBeTrue();
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("index");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("status")).shouldBeEqual(200);

    }

    @Test
    public void shouldPrintControllerException() throws IOException, ServletException {

        request.setServletPath("/logging/error");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        //Line 0
        Map log0 = JsonHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        Map message = (Map) log0.get("message");
        the(message.get("info")).shouldBeEqual("executing controller");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("error");
        the(message.get("method")).shouldBeEqual("GET");

        //Line 1
        Map log1 = JsonHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("ERROR");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        //{"success": false,"controller":"app.controllers.LoggingController","action":"error","duration_millis":9,"method":"GET","status":500}}
        message = (Map) log1.get("message");
        the(message.get("success")).shouldBeFalse();
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("status")).shouldBeEqual(500);

        //Line 2
        Map log2 = JsonHelper.toMap(logs[2]);
        the(log2.get("level")).shouldBeEqual("ERROR");
        the(log2.get("timestamp")).shouldNotBeNull();
        the(log2.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        the(log2.get("message")).shouldBeEqual("ERROR!");

        Map exception = (Map) log2.get("exception");
        the(exception.get("message")).shouldBeEqual("blah!");
        the(exception.get("stacktrace")).shouldContain("java.lang.RuntimeException: blah!\njava.lang.RuntimeException: blah!\n\tat app.controllers.LoggingController.error");

        //Line 3
        Map log3 = JsonHelper.toMap(logs[3]);
        the(log3.get("level")).shouldBeEqual("INFO");
        the(log3.get("timestamp")).shouldNotBeNull();
        the(log3.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log3.get("message")).shouldBeEqual("rendering template: '/system/error' with layout: '/layouts/default_layout");
    }

    @Test
    public void shouldPrintSystem404IfActionMissing() throws IOException, ServletException {

        request.setServletPath("/logging/notfound");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        //Line 0
        Map log0 = JsonHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");

        Map message = (Map) log0.get("message");
        the(message.get("info")).shouldBeEqual("executing controller");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("notfound");
        the(message.get("method")).shouldBeEqual("GET");

        //Line 1
        Map log1 = JsonHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("ERROR");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        message = (Map) log1.get("message");
        the(message.get("success")).shouldBeFalse();
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("notfound");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("status")).shouldBeEqual(404);
        the(message.get("error")).shouldBeEqual("java.lang.NoSuchMethodException: app.controllers.LoggingController.notfound(); app.controllers.LoggingController.notfound()");

        //Line 2
        Map log2 = JsonHelper.toMap(logs[2]);
        the(log2.get("level")).shouldBeEqual("INFO");
        the(log2.get("timestamp")).shouldNotBeNull();
        the(log2.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log2.get("message")).shouldBeEqual("rendering template: '/system/404' with layout: '/layouts/default_layout");
    }

    @Test
    public void shouldPrintSystem404IfControllerMissing() throws IOException, ServletException {

        request.setServletPath("/fake11");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));

        //Line0
        Map log1 = JsonHelper.toMap(logs[0]);
        the(log1.get("level")).shouldBeEqual("ERROR");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        Map message = (Map) log1.get("message");
        the(message.get("success")).shouldBeFalse();
        //"message":{"success": true,"controller":"","action":"","duration_millis":8,"method":"GET","info":"org.javalite.activeweb.ClassLoadException: java.lang.ClassNotFoundException: app.controllers.Fake11Controller","status":404}}
        the(message.get("controller")).shouldBeEqual("");
        the(message.get("action")).shouldBeEqual("");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("method")).shouldBeEqual("GET");
        the(message.get("status")).shouldBeEqual(404);
        the(message.get("error")).shouldBeEqual("java.lang.ClassNotFoundException: app.controllers.Fake11Controller");

        //Line 1
        Map log2 = JsonHelper.toMap(logs[1]);
        the(log2.get("level")).shouldBeEqual("INFO");
        the(log2.get("timestamp")).shouldNotBeNull();
        the(log2.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log2.get("message")).shouldBeEqual("rendering template: '/system/404' with layout: '/layouts/default_layout");
    }

    @Test
    public void shouldPrintSystem404IfViewMissing() throws IOException, ServletException {

        request.setServletPath("/logging/no-view");
        request.setMethod("GET");
        dispatcher.doFilter(request, response, filterChain);

        String out = SystemStreamUtil.getSystemOut();
        String[] logs = Util.split(out, System.getProperty("line.separator"));






//
//
//        {"level":"INFO","timestamp":"Wed Feb 15 19:04:23 CST 2017","thread":"main","logger":"org.javalite.activeweb.freemarker.FreeMarkerTemplateManager","message":"rendering template: '/system/404' with layout: '/layouts/default_layout"}


        //Line 0
        Map log0 = JsonHelper.toMap(logs[0]);
        the(log0.get("level")).shouldBeEqual("INFO");
        the(log0.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        the(log0.get("timestamp")).shouldNotBeNull();;

        Map message = (Map) log0.get("message");
        the(message.get("info")).shouldBeEqual("executing controller");
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("no-view");
        the(message.get("method")).shouldBeEqual("GET");

        //rendering template: '/logging/no-view' with layout: '/layouts/default_layout"}
        //Line 1
        Map log1 = JsonHelper.toMap(logs[1]);
        the(log1.get("level")).shouldBeEqual("INFO");
        the(log1.get("timestamp")).shouldNotBeNull();
        the(log1.get("logger")).shouldBeEqual("org.javalite.activeweb.freemarker.FreeMarkerTemplateManager");
        the(log1.get("message")).shouldBeEqual("rendering template: '/logging/no-view' with layout: '/layouts/default_layout");


        //Line 2
        Map log2 = JsonHelper.toMap(logs[2]);
        the(log2.get("level")).shouldBeEqual("ERROR");
        the(log2.get("timestamp")).shouldNotBeNull();
        the(log2.get("logger")).shouldBeEqual("org.javalite.activeweb.RequestDispatcher");
        message = (Map) log2.get("message");

        the(message.get("success")).shouldBeEqual(false);
        the(message.get("controller")).shouldBeEqual("app.controllers.LoggingController");
        the(message.get("action")).shouldBeEqual("no-view");
        the(message.get("error")).shouldBeEqual("Failed to find template: 'src/test/views/logging/no-view.ftl', with layout: 'src/test/views/layouts/default_layout'");

    }
}