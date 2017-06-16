package apps.pagerender.vue.structure.page;

import javax.script.Bindings;

import org.apache.sling.scripting.sightly.pojo.Use;
import org.apache.sling.api.resource.Resource;

import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingScriptHelper;

public class Helper implements Use {

    private Object model;
    private String siteRootPath;

    public String getHello() {
        return "hello";
    }

    public Object getModel() {
        return model;
    }

    public String getSiteRootPath() {
        return siteRootPath;
    }

    public String getModelClass() {
        return model.getClass().toString();
    }

    public void init(Bindings bindings) {
        Resource resource = (Resource) bindings.get("resource");
        SlingHttpServletRequest request = (SlingHttpServletRequest) bindings.get("request");
        SlingScriptHelper sling = (SlingScriptHelper) bindings.get("sling");

        String path = resource.getPath();
        if(path.startsWith("/content/sites/")) {
            path = path.substring("/content/sites/".length());
        } else if(path.startsWith("/content/templates/")) {
            path = path.substring("/content/templates/".length());
        }
        int slash = path.indexOf("/");
        String siteName = slash > 0 ? path.substring(0, path.indexOf("/")) : path;
        siteRootPath = "/content/sites/"+siteName;

        try {
            model = sling.getService(ModelFactory.class).getModelFromResource(resource);
        } catch(Throwable t) {
            model = sling.getService(ModelFactory.class).getModelFromRequest(request);
        }
    }
}