package jetbrains.buildServer.clouds.kubernetes.web;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectionCheckResult;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectorImpl;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.internal.PluginPropertiesUtil;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeProfileEditController extends BaseFormXmlController {
    private final static Logger LOG = Logger.getInstance(KubeProfileEditController.class.getName());

    public static final String EDIT_KUBE_HTML = "editKube.html";
    private final String myPath;

    private final PluginDescriptor myPluginDescriptor;
    private final AgentPoolManager myAgentPoolManager;
    private final KubeAuthStrategyProvider myAuthStrategyProvider;
    private final BuildAgentPodTemplateProviders myPodTemplateProviders;

    public KubeProfileEditController(@NotNull final SBuildServer server,
                                     @NotNull final WebControllerManager web,
                                     @NotNull final PluginDescriptor pluginDescriptor,
                                     @NotNull final AgentPoolManager agentPoolManager,
                                     @NotNull final KubeAuthStrategyProvider authStrategyProvider,
                                     @NotNull final BuildAgentPodTemplateProviders podTemplateProviders) {
        super(server);
        myPluginDescriptor = pluginDescriptor;
        myPath = pluginDescriptor.getPluginResourcesPath(EDIT_KUBE_HTML);
        myAgentPoolManager = agentPoolManager;
        myAuthStrategyProvider = authStrategyProvider;
        myPodTemplateProviders = podTemplateProviders;
        web.registerController(myPath, this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editProfile.jsp"));
        Map<String, Object> model = modelAndView.getModel();
        model.put("testConnectionUrl", myPath + "?testConnection=true");
        final String projectId = httpServletRequest.getParameter("projectId");
        model.put("agentPools", myAgentPoolManager.getProjectOwnedAgentPools(projectId));
        model.put("authStrategies", myAuthStrategyProvider.getAll());
        model.put("podTemplateProviders", myPodTemplateProviders.getAll());
        return modelAndView;
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Element xmlResponse) {
        BasePropertiesBean propsBean =  new BasePropertiesBean(null);
        PluginPropertiesUtil.bindPropertiesFromRequest(request, propsBean, true);
        final Map<String, String> props = propsBean.getProperties();
        final String authStrategy = props.get(KubeParametersConstants.AUTH_STRATEGY);

        if(Boolean.parseBoolean(request.getParameter("testConnection"))){
            KubeApiConnection connectionSettings = new KubeApiConnection() {
                @NotNull
                @Override
                public String getApiServerUrl() {
                    return props.get(KubeParametersConstants.API_SERVER_URL);
                }

                @Nullable
                @Override
                public String getNamespace() {
                    return props.get(KubeParametersConstants.KUBERNETES_NAMESPACE);
                }

                @Nullable
                @Override
                public String getCustomParameter(@NotNull String parameterName) {
                    return props.containsKey(parameterName) ? props.get(parameterName) : props.get(SECURE_PROPERTY_PREFIX + parameterName);
                }
            };
            try {
                KubeApiConnectorImpl apiConnector = KubeApiConnectorImpl.create(connectionSettings, myAuthStrategyProvider.get(authStrategy));
                KubeApiConnectionCheckResult connectionCheckResult = apiConnector.testConnection();
                if(!connectionCheckResult.isSuccess()){
                    final ActionErrors errors = new ActionErrors();
                    errors.addError("connection", connectionCheckResult.getMessage());
                    writeErrors(xmlResponse, errors);
                }
            } catch (Exception ex){
                LOG.debug(ex);
                final ActionErrors errors = new ActionErrors();
                errors.addError("connection", ex.getMessage());
                writeErrors(xmlResponse, errors);
            }
        }
    }
}
