package ru.masterdm.crs.web.model.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Default;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.QueryParam;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.web.Attributes;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Include;
import org.zkoss.zul.Textbox;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientCategoryAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientCountryAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientDepartmentAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientIndustryAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientInnAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientOgrnAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientOpfAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientSegmentAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientTypeAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FavoritesService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.web.domain.BreadCrumb;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.service.EntityMetaUiService;

/**
 * Navigation viewModel.
 * @author Alexey Chalov
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class NavigationModel {

    @WireVariable("config")
    private Properties config;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private SecurityService securityService;
    @WireVariable
    protected FavoritesService favoritesService;
    @WireVariable
    private Page initialPage;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable
    protected EntityMetaUiService entityMetaUiService;

    private String page;
    private String buildBranch;
    private String projectInfo;
    private boolean homePage = true;
    private boolean adminMenu;
    private boolean freezeAdminMenu;
    protected EntityMeta clientEntityMeta;
    protected EntityMeta calculationEntityMeta;
    protected EntityMeta clientGroupEntityMeta;
    protected EntityMeta userEntityMeta;
    protected EntityMeta clientTypeEntityMeta;
    private String searchString;
    private String currentBookmark;
    private int stringMaxLength;

    private List<BreadCrumb> breadCrumbs = new ArrayList<>();

    private static final String SEPARATOR_ID = "/";

    /**
     * Performs navigation.
     * @param currentPage current page logical name
     * @param targetPage target page logical name
     * @param collapseAdminMenu collapse admin menu
     */
    @Command
    @SmartNotifyChange({"page", "adminMenu", "homePage"})
    public void navigate(@ContextParam(ContextType.PAGE) Page currentPage, @BindingParam(value = "targetPage") String targetPage,
                         @BindingParam("collapseAdminMenu") @Default("false") boolean collapseAdminMenu) {
        hideAdminMenu();
        homePage = targetPage.equals("welcome");
        if (collapseAdminMenu) {
            adminMenu = false;
        }
        breadCrumbs.clear();
        includePage(currentPage, targetPage);
    }

    /**
     * Performs navigation.
     * @param currentPage current page logical name
     * @param targetPage target page logical name
     */
    @GlobalCommand
    @SmartNotifyChange({"page", "homePage"})
    public void navigateGlobal(@ContextParam(ContextType.PAGE) Page currentPage, @BindingParam(value = "targetPage") String targetPage) {
        homePage = false;
        if (breadCrumbs.stream().anyMatch(breadCrumb -> breadCrumb.getPage().equals(targetPage))) {
            for (int i = breadCrumbs.size() - 1; i >= 0; i--) {
                String page = breadCrumbs.get(i).getPage();
                breadCrumbs.remove(breadCrumbs.get(i));
                if (i > 0) {
                    String key = breadCrumbs.get(i - 1).getKey();
                    String keyNew = (String) Executions.getCurrent().getAttribute("key");
                    if (keyNew != null && breadCrumbs.get(i - 1).getPage().equals(targetPage)) {
                        breadCrumbs.get(i - 1).setKey(keyNew);
                        key = keyNew;
                    }
                    if (page.equals(targetPage)) {
                        Executions.getCurrent().setAttribute("key", key);
                        if (this.page.equals("calc.edit_calculation"))
                            Executions.getCurrent().setAttribute("mode", true);
                        break;
                    }
                }
            }
        } else if (page != null && !page.equals("welcome")) {
            String key = (String) Executions.getCurrent().getAttribute("key");
            breadCrumbs.add(new BreadCrumb(page, getPageShortTitle(), key));
        }
        includePage(currentPage, targetPage);
    }

    /**
     * Navigates backward.
     */
    @GlobalCommand
    public void navigateBackward() {
        if (breadCrumbs.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("targetPage", breadCrumbs.get(breadCrumbs.size() - 1).getPage());
            BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
        }
    }

    /**
     * Includes page.
     * @param currentPage current page.
     * @param targetPage target page
     */
    private void includePage(Page currentPage, String targetPage) {
        page = targetPage;
        Include include = (Include) Selectors.iterable(currentPage, "#pageInclude").iterator().next();
        // Resets include to refresh inclusion with the same source
        include.setSrc(null);
        include.setSrc(getPageName());
        include.getPage().setTitle(getPageTitle());
        String key = (String) Executions.getCurrent().getAttribute("key");
        key = SEPARATOR_ID.concat((key != null) ? key : "");
        currentBookmark = targetPage.concat(key);
        Executions.getCurrent().getDesktop().setBookmark(currentBookmark);
    }

    /**
     * ViewModel initialization method.
     * @param page page
     */
    @Init
    public void init(@QueryParam("page") String page) {
        if (page != null && !page.trim().equals("")) {
            this.page = page;
        }
        stringMaxLength = Integer.parseInt(webConfig.getProperty("stringMaxLength"));
        StringBuilder projectInfo = new StringBuilder(config.getProperty("project.version")).append("/");
        projectInfo.append(config.getProperty("project.build.time")).toString();
        this.projectInfo = projectInfo.toString();
        buildBranch = config.getProperty("build.branch");
        initialPage.setTitle(getPageTitle());
        Object navigationModel = this;
        initialPage.addEventListener(Events.ON_BOOKMARK_CHANGE, event -> {
            String bookmark = Executions.getCurrent().getDesktop().getBookmark();
            if (ObjectUtils.notEqual(bookmark, currentBookmark)) {
                currentBookmark = bookmark;
                int n = bookmark.lastIndexOf(SEPARATOR_ID);
                String targetPage = (n >= 0) ? bookmark.substring(0, n) : "welcome";
                String key = (n >= 0) ? bookmark.substring(n + 1, bookmark.length()) : "";
                if (!key.isEmpty()) {
                    Executions.getCurrent().setAttribute("key", key);
                }
                navigateGlobal(initialPage, targetPage);
                BindUtils.postNotifyChange(null, null, navigationModel, "*");
            }
        });
    }

    /**
     * Returns logical page name.
     * @return logical page name
     */
    public String getPage() {
        return getPageName();
    }

    /**
     * Returns project info.
     * @return project info
     */
    public String getProjectInfo() {
        return projectInfo;
    }

    /**
     * Returns build branch.
     * @return build branch
     */
    public String getBuildBranch() {
        return buildBranch;
    }

    /**
     * Returns page name.
     * @return page name
     */
    public String getPageName() {
        return page != null ? pages.getProperty(page) : pages.getProperty("welcome");
    }

    /**
     * Returns page title.
     * @return page title
     */
    private String getPageTitle() {
        String pageName = getPageName();
        pageName = pageName.split("/")[pageName.split("/").length - 1];
        pageName = pageName.replaceAll(".zul", "");
        String entityName = "";
        Entity entity = (Entity) Executions.getCurrent().getAttribute("copiedCalculation");
        entity = (entity == null) ? (Entity) Executions.getCurrent().getAttribute("entity") : entity;
        if (entity != null) {
            if (entity.getMeta().getKey().equals(Calculation.METADATA_KEY)) {
                Calculation calculation = (Calculation) entity;
                if (calculation.getClientGroup() != null)
                    entity = calculation.getClientGroup();
                else if (calculation.getClient() != null)
                    entity = calculation.getClient();
            }
            switch (entity.getMeta().getKey()) {
                case ClientGroupAttributeMeta.METADATA_KEY:
                    entityName = ((MultilangAttribute) entity.getAttribute(ClientGroupAttributeMeta.NAME.getKey()))
                            .getValue(userProfile.getLocale());
                    break;
                case ClientAttributeMeta.METADATA_KEY:
                    entityName = ((MultilangAttribute) entity.getAttribute(ClientAttributeMeta.NAME.getKey())).getValue(userProfile.getLocale());
                    break;
                default:
            }
        }
        return String.format("%s | %s %s", Labels.getLabel("layout_title"), Labels.getLabel(pageName + "_title"), entityName);
    }

    /**
     * Returns page title without System's title.
     * @return page short title
     */
    private String getPageShortTitle() {
        String pageName = getPageName();
        pageName = pageName.split("/")[pageName.split("/").length - 1];
        pageName = pageName.replaceAll(".zul", "");
        return Labels.getLabel(pageName + "_title");
    }

    /**
     * Keep page keep alive interval.
     * @return page keep alive interval
     */
    public Long getSessionKeepAliveInterval() {
        return Long.parseLong(webConfig.getProperty("session.keep.alive.interval.minutes")) * DateUtils.MILLIS_PER_MINUTE;
    }

    /**
     * Keep alive.
     */
    @Command
    public void keepAlive() {
    }

    /**
     * Returns is admin menu visible.
     * @return is admin menu visible
     */
    public boolean isAdminMenu() {
        return adminMenu;
    }

    /**
     * Shows/hides menu for admin.
     */
    @Command
    @SmartNotifyChange({"adminMenu", "homePage"})
    public void showAdminMenu() {
        adminMenu = !adminMenu;
    }

    /**
     * Hides admin menu.
     */
    @Command
    @SmartNotifyChange("adminMenu")
    public void hideAdminMenu() {
        if (!freezeAdminMenu)
            adminMenu = false;
    }

    /**
     * Sets locale.
     * @param language language
     */
    @Command
    public void setLocale(@BindingParam("language") String language) {
        Executions.getCurrent().getSession().setAttribute(Attributes.PREFERRED_LOCALE, new Locale(language));
        //reload the same page
        Executions.sendRedirect(null);
    }

    /**
     * Returns is home page.
     * @return is home page
     */
    public boolean isHomePage() {
        return homePage;
    }

    /**
     * Returns search string.
     * @return search string
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * Sets search string.
     * @param searchString search string
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * Searches.
     */
    @Command
    public void search() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "search_result");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns is admin menu frozen.
     * @return is admin menu frozen
     */
    public boolean isFreezeAdminMenu() {
        return freezeAdminMenu;
    }

    /**
     * Freeze admin menu.
     */
    @Command
    @SmartNotifyChange("freezeAdminMenu")
    public void freezeAdminMenu() {
        freezeAdminMenu = !freezeAdminMenu;
    }

    /**
     * Focus pointer to the context textbox.
     * @param textbox context textbox
     */
    @Command
    public void focus(@ContextParam(ContextType.COMPONENT) Textbox textbox) {
        textbox.focus();
    }

    /**
     * Returns current user full name.
     * @return current user full name
     */
    public String getCurrentUser() {
        return securityService.getCurrentUser().getFullName();
    }

    /**
     * Returns client entity meta.
     * @return client entity meta
     */
    public EntityMeta getClientEntityMeta() {
        if (clientEntityMeta == null) {
            clientEntityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, null);
        }
        return clientEntityMeta;
    }

    /**
     * Returns calculation entity meta.
     * @return calculation entity meta
     */
    public EntityMeta getCalculationEntityMeta() {
        if (calculationEntityMeta == null)
            calculationEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
        return calculationEntityMeta;
    }

    /**
     * Returns client group entity meta.
     * @return client group entity meta
     */
    public EntityMeta getClientGroupEntityMeta() {
        if (clientGroupEntityMeta == null) {
            clientGroupEntityMeta = entityMetaService.getEntityMetaByKey(ClientGroupAttributeMeta.METADATA_KEY, null);
        }
        return clientGroupEntityMeta;
    }

    /**
     * Returns user entity meta.
     * @return user entity meta
     */
    public EntityMeta getUserEntityMeta() {
        if (userEntityMeta == null) {
            userEntityMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
        }
        return userEntityMeta;
    }

    /**
     * Returns client type entity meta.
     * @return client type entity meta
     */
    public EntityMeta getClientTypeEntityMeta() {
        if (clientTypeEntityMeta == null) {
            clientTypeEntityMeta = entityMetaService.getEntityMetaByKey(ClientTypeAttributeMeta.METADATA_KEY, null);
        }
        return clientTypeEntityMeta;
    }

    /**
     * Returns client attribute label.
     * @param key key
     * @return client attribute label
     */
    public String getClientAttributeLabel(String key) {
        return getClientAttributeMeta(key).getName().getDescription(userProfile.getLocale());
    }

    /**
     * Returns client group attribute label.
     * @param key key
     * @return client group attribute label
     */
    public String getClientGroupAttributeLabel(String key) {
        return getClientGroupAttributeMeta(key).getName().getDescription(userProfile.getLocale());
    }

    /**
     * Returns user attribute label.
     * @param key key
     * @return user attribute label
     */
    public String getUserAttributeLabel(String key) {
        return getUserAttributeMeta(key).getName().getDescription(userProfile.getLocale());
    }

    /**
     * Returns client attribute meta.
     * @param key key
     * @return client attribute meta
     */
    public AttributeMeta getClientAttributeMeta(String key) {
        return getClientEntityMeta().getAttributeMetadata(ClientAttributeMeta.valueOf(key).getKey());
    }

    /**
     * Returns client group attribute meta.
     * @param key key
     * @return client group attribute meta
     */
    public AttributeMeta getClientGroupAttributeMeta(String key) {
        return getClientGroupEntityMeta().getAttributeMetadata(ClientGroupAttributeMeta.valueOf(key).getKey());
    }

    /**
     * Returns calculation attribute meta.
     * @param attributeKey key
     * @param attributeAttributeKey attribute key
     * @return calculation attribute meta
     */
    public AttributeMeta getCalculationAttributeAttributeMeta(String attributeKey, String attributeAttributeKey) {
        return getCalculationEntityMeta()
                .getAttributeMetadata(Calculation.CalculationAttributeMeta.valueOf(attributeKey).getKey())
                .getAttributeMetadata(Calculation.CalculationClientAttributeMeta.valueOf(attributeAttributeKey).getKey());
    }

    /**
     * Returns user attribute meta.
     * @param key key
     * @return user attribute meta
     */
    public AttributeMeta getUserAttributeMeta(String key) {
        return getUserEntityMeta().getAttributeMetadata(User.UserAttributeMeta.valueOf(key).getKey());
    }

    /**
     * Returns client INN attribute meta key.
     * @param key key
     * @return client INN attribute meta key
     */
    public String getClientInnAttributeKey(String key) {
        return ClientInnAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client OGRN attribute meta key.
     * @param key key
     * @return client OGRN attribute meta key
     */
    public String getClientOgrnAttributeKey(String key) {
        return ClientOgrnAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client segment attribute meta key.
     * @param key key
     * @return client segment attribute meta key
     */
    public String getClientSegmentAttributeKey(String key) {
        return ClientSegmentAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client industry attribute meta key.
     * @param key key
     * @return client industry attribute meta key
     */
    public String getClientIndustryAttributeKey(String key) {
        return ClientIndustryAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client category attribute meta key.
     * @param key key
     * @return client category attribute meta key
     */
    public String getClientCategoryAttributeKey(String key) {
        return ClientCategoryAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns calculation client attribute meta key.
     * @param key key
     * @return calculation client attribute meta key
     */
    public String getCalculationClientAttributeKey(String key) {
        return Calculation.CalculationClientAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client type attribute meta key.
     * @param key key
     * @return client type attribute meta key
     */
    public String getClientTypeAttributeKey(String key) {
        return ClientTypeAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client OPF attribute meta key.
     * @param key key
     * @return client OPF attribute meta key
     */
    public String getClientOpfAttributeKey(String key) {
        return ClientOpfAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client country attribute meta key.
     * @param key key
     * @return client country attribute meta key
     */
    public String getClientCountryAttributeKey(String key) {
        return ClientCountryAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client cp department attribute meta key.
     * @param key key
     * @return client cp department attribute meta key
     */
    public String getClientCpDepartmentAttributeKey(String key) {
        return ClientDepartmentAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns client department attribute meta key.
     * @param key key
     * @return client department attribute meta key
     */
    public String getClientDepartmentAttributeKey(String key) {
        return Department.DepartmentAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns breadcrumbs.
     * @return breadcrumbs
     */
    public List<BreadCrumb> getBreadCrumbs() {
        return breadCrumbs;
    }

    /**
     * Edits departments.
     */
    @Command
    @SmartNotifyChange({"page", "adminMenu", "homePage"})
    public void editDepartments() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Department.METADATA_KEY, null);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.edit_departments");
        Executions.getCurrent().setAttribute("entityMetaKey", entityMeta.getKey());
        Executions.getCurrent().setAttribute("key", entityMeta.getKey());
        Executions.getCurrent().setAttribute("entityMetaName", entityMeta.getName().getDescription(userProfile.getLocale()));
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
        adminMenu = false;
    }

    /**
     * Edits users.
     */
    @Command
    @SmartNotifyChange({"page", "adminMenu", "homePage"})
    public void editUsers() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.edit_users");
        Executions.getCurrent().setAttribute("entityMetaKey", entityMeta.getKey());
        Executions.getCurrent().setAttribute("key", entityMeta.getKey());
        Executions.getCurrent().setAttribute("entityMetaName", entityMeta.getName().getDescription(userProfile.getLocale()));
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
        adminMenu = false;
    }

    /**
     * Edits roles.
     */
    @Command
    @SmartNotifyChange({"page", "adminMenu", "homePage"})
    public void editRoles() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Role.METADATA_KEY, null);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.edit_roles");
        Executions.getCurrent().setAttribute("entityMetaKey", entityMeta.getKey());
        Executions.getCurrent().setAttribute("key", entityMeta.getKey());
        Executions.getCurrent().setAttribute("entityMetaName", entityMeta.getName().getDescription(userProfile.getLocale()));
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
        adminMenu = false;
    }

    /**
     * Returns string max length.
     * @return string max length
     */
    public int getStringMaxLength() {
        return stringMaxLength;
    }

    /**
     * Edits permissions.
     */
    @Command
    @SmartNotifyChange({"page", "adminMenu", "homePage"})
    public void editPermissions() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", pages.getProperty("entity.edit_permissions"));
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
        adminMenu = false;
    }

    /**
     * Returns is permitted for entity type.
     * @param key key
     * @param action action
     * @return is permitted for entity type
     */
    public boolean isPermittedForEntityType(String key, String action) {
        return securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                        entityMetaUiService.getPermissionEntity(EntityType.valueOf(key)),
                                                        BusinessAction.Action.valueOf(action));
    }

    /**
     * Returns is permitted.
     * @param key key
     * @param action action
     * @return is permitted
     */
    public boolean isPermitted(String key, String action) {
        return securityService.isPermitted(securityService.getCurrentUser(),
                                           entityMetaService.getEntityMetaByKey(key, null),
                                           BusinessAction.Action.valueOf(action));
    }

    /**
     * Returns is admin menu allowed.
     * @return is admin menu allowed
     */
    public boolean isAdminMenuAllowed() {
        return securityService.isPermitted(securityService.getCurrentUser(), entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null),
                                           BusinessAction.Action.VIEW)
               || securityService.isPermitted(securityService.getCurrentUser(), entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null),
                                              BusinessAction.Action.VIEW)
               || securityService.isPermitted(securityService.getCurrentUser(), entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, null),
                                              BusinessAction.Action.VIEW)
               || securityService.isPermitted(securityService.getCurrentUser(), entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null),
                                              BusinessAction.Action.VIEW)
               || securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                           entityMetaUiService.getPermissionEntity(EntityType.INPUT_FORM),
                                                           BusinessAction.Action.VIEW)
               || securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                           entityMetaUiService.getPermissionEntity(EntityType.CLASSIFIER),
                                                           BusinessAction.Action.VIEW)
               || securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                           entityMetaUiService.getPermissionEntity(EntityType.DICTIONARY),
                                                           BusinessAction.Action.VIEW);
    }
}
