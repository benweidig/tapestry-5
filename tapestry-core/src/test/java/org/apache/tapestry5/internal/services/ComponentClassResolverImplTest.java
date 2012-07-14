package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.LibraryMapping;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.isA;

public class ComponentClassResolverImplTest extends InternalBaseTestCase
{
    private static final String APP_ROOT_PACKAGE = "org.example.app";

    private static final List<LibraryMapping> APP_ROOT_PACKAGE_MAPPINGS = Arrays.asList(new LibraryMapping("",
            APP_ROOT_PACKAGE));

    private static final String CORE_PREFIX = "core";

    private static final String CORE_ROOT_PACKAGE = "org.apache.tapestry5.corelib";

    private static final String LIB_PREFIX = "lib";

    private static final String LIB_ROOT_PACKAGE = "org.example.lib";

    private ComponentClassResolverImpl create(Logger logger, ClassNameLocator locator, LibraryMapping... mappings)
    {
        List<LibraryMapping> full = F.flow(APP_ROOT_PACKAGE_MAPPINGS).concat(F.flow(mappings)).toList();

        return new ComponentClassResolverImpl(logger, locator, "Start", full);
    }

    private Logger compliantLogger()
    {
        Logger logger = mockLogger();

        logger.info(EasyMock.isA(String.class));

        EasyMock.expectLastCall().atLeastOnce();

        return logger;
    }

    @Test
    public void simple_page_name()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), className);

        verify();
    }

    /**
     * TAPESTRY-1923
     */
    @Test
    public void get_page_names()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", APP_ROOT_PACKAGE + ".pages.SimplePage",
                APP_ROOT_PACKAGE + ".pages.nested.Other", APP_ROOT_PACKAGE + ".pages.nested.NestedPage",
                APP_ROOT_PACKAGE + ".pages.nested.NestedIndex");

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        List<String> pageNames = resolver.getPageNames();

        assertListsEquals(pageNames, "SimplePage", "nested/Index", "nested/Other", "nested/Page");

        verify();
    }

    /**
     * TAPESTRY-1541
     */
    @Test
    public void page_name_matches_containing_folder_name()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.admin.product.ProductAdmin";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("admin/product/ProductAdmin"), className);

        verify();
    }

    @Test
    public void canonicalize_existing_page_name()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.canonicalizePageName("simplepage"), "SimplePage");

        verify();
    }

    @Test
    public void canonicalize_start_page()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.HomePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = new ComponentClassResolverImpl(logger, locator, "HomePage",
                APP_ROOT_PACKAGE_MAPPINGS);

        assertEquals(resolver.canonicalizePageName("HomePage"), "HomePage");
        assertEquals(resolver.canonicalizePageName(""), "HomePage");
        assertTrue(resolver.isPageName("HomePage"));

        verify();
    }

    @Test
    public void start_page_in_subfolder()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.sub.HomePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = new ComponentClassResolverImpl(logger, locator, "HomePage",
                APP_ROOT_PACKAGE_MAPPINGS);

        assertEquals(resolver.canonicalizePageName("sub/HomePage"), "sub/HomePage");
        assertEquals(resolver.canonicalizePageName("sub"), "sub/HomePage");
        assertTrue(resolver.isPageName("sub/HomePage"));

        verify();
    }

    /**
     * TAP5-1444
     */
    @Test
    public void index_page_precedence()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String[] classNames =
                {APP_ROOT_PACKAGE + ".pages.sub.HomePage", APP_ROOT_PACKAGE + ".pages.sub.SubIndex"};

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", classNames);

        replay();

        List<LibraryMapping> mappings = APP_ROOT_PACKAGE_MAPPINGS;

        ComponentClassResolver resolver = new ComponentClassResolverImpl(logger, locator, "HomePage", mappings);

        assertTrue(resolver.isPageName("sub/HomePage"));
        assertTrue(resolver.isPageName("sub/subIndex"));
        assertEquals(resolver.resolvePageNameToClassName("sub/HomePage"), APP_ROOT_PACKAGE + ".pages.sub.HomePage");
        assertEquals(resolver.resolvePageNameToClassName("sub/SubIndex"), APP_ROOT_PACKAGE + ".pages.sub.SubIndex");
        assertEquals(resolver.resolvePageNameToClassName("sub/Index"), APP_ROOT_PACKAGE + ".pages.sub.SubIndex");
        assertEquals(resolver.resolvePageNameToClassName("sub"), APP_ROOT_PACKAGE + ".pages.sub.SubIndex");

        verify();
    }

    @Test
    public void page_name_in_subfolder()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.subfolder.NestedPage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("subfolder/NestedPage"), className);

        verify();
    }

    @Test
    public void lots_of_prefixes_and_suffixes_stripped()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.admin.edit.AdminUserEdit";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("admin/edit/User"), className);
        assertEquals(resolver.resolvePageNameToClassName("admin/edit/AdminUserEdit"), className);

        verify();
    }

    @Test
    public void page_in_subfolder()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.subfolder.NestedPage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("subfolder/NestedPage"), className);

        verify();
    }

    @Test
    public void subfolder_name_as_classname_prefix_is_stripped()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.foo.FooBar";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("foo/Bar"), className);

        verify();
    }

    @Test
    public void core_prefix_stripped_from_exception_message()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", CORE_ROOT_PACKAGE + ".pages.Fred",
                CORE_ROOT_PACKAGE + ".pages.Barney");
        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", APP_ROOT_PACKAGE + ".pages.Wilma",
                APP_ROOT_PACKAGE + ".pages.Betty");

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.resolvePageNameToClassName("Unknown");
            unreachable();
        } catch (UnknownValueException ex)
        {
            assertEquals(ex.getMessage(), "Unable to resolve \'Unknown\' to a page class name.");
            assertEquals(ex.getAvailableValues().toString(), "AvailableValues[Page names: Barney, Betty, Fred, Wilma]");
        }

        verify();
    }

    @Test
    public void is_page_name()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertTrue(resolver.isPageName("SimplePage"));
        assertTrue(resolver.isPageName("simplepage"));
        assertFalse(resolver.isPageName("UnknownPage"));

        verify();
    }

    @Test
    public void index_page_name_at_root()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = APP_ROOT_PACKAGE + ".pages.Index";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertTrue(resolver.isPageName("Index"));
        assertTrue(resolver.isPageName(""));

        verify();
    }

    @Test
    public void is_page_name_for_core_page()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String className = CORE_ROOT_PACKAGE + ".pages.MyCorePage";

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        // Can look like an application page, but still resolves to the core library class name.

        assertTrue(resolver.isPageName("MyCorePage"));

        // Or we can give it its true name

        assertTrue(resolver.isPageName("core/mycorepage"));

        assertFalse(resolver.isPageName("UnknownPage"));

        verify();
    }

    protected final ClassNameLocator newClassNameLocator()
    {
        ClassNameLocator locator = newMock(ClassNameLocator.class);

        stub_locateComponentClassNames(locator);

        return locator;
    }

    private void stub_locateComponentClassNames(ClassNameLocator locator)
    {
        Collection<String> noMatches = Collections.emptyList();

        expect(locator.locateClassNames(isA(String.class))).andStubReturn(noMatches);
    }

    protected final void train_locateComponentClassNames(ClassNameLocator locator, String packageName,
                                                         String... classNames)
    {
        expect(locator.locateClassNames(packageName)).andReturn(Arrays.asList(classNames));
    }

    @Test
    public void class_name_to_simple_page_name()
    {
        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageClassNameToPageName(className), "SimplePage");

        verify();
    }

    /**
     * All of the caches are handled identically, so we just test the pages for caching.
     */
    @Test
    public void resolved_page_names_are_cached()
    {
        String pageClassName = APP_ROOT_PACKAGE + ".pages.SimplePage";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", pageClassName);

        replay();

        ComponentClassResolverImpl resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), pageClassName);

        verify();

        // No more training, because it's already cached.

        replay();

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), pageClassName);

        verify();

        // After clearing the cache, redoes the work.

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", pageClassName);
        stub_locateComponentClassNames(locator);

        replay();

        resolver.objectWasInvalidated();

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), pageClassName);

        verify();
    }

    @Test
    public void page_found_in_core_lib()
    {
        String className = CORE_ROOT_PACKAGE + ".pages.MyCorePage";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("MyCorePage"), className);

        verify();
    }

    @Test
    public void page_class_name_resolved_to_core_page()
    {
        String className = CORE_ROOT_PACKAGE + ".pages.MyCorePage";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageClassNameToPageName(className), "core/MyCorePage");

        verify();
    }

    @Test
    public void page_found_in_library()
    {
        String className = LIB_ROOT_PACKAGE + ".pages.MyLibPage";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/MyLibPage"), className);

        verify();
    }


    @Test
    public void lookup_by_logical_name_is_case_insensitive()
    {
        String className = LIB_ROOT_PACKAGE + ".pages.MyLibPage";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/MyLibPage"), className);

        verify();
    }

    @Test
    public void name_stripping_includes_library_folder()
    {
        String className = LIB_ROOT_PACKAGE + ".pages.LibPage";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/Page"), className);

        verify();
    }

    @Test
    public void class_name_does_not_resolve_to_page_name()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        String className = LIB_ROOT_PACKAGE + ".pages.LibPage";

        try
        {
            resolver.resolvePageClassNameToPageName(className);
            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Unable to resolve class name " + className + " to a logical page name.");
        }

        verify();
    }

    @Test
    public void page_name_to_canonicalize_does_not_exist()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", APP_ROOT_PACKAGE + ".pages.Start");

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.canonicalizePageName("MissingPage");
            unreachable();
        } catch (UnknownValueException ex)
        {
            assertEquals(ex.getMessage(), "Unable to resolve 'MissingPage' to a known page name.");
        }

        verify();
    }

    @Test
    public void class_name_not_in_a_pages_package()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        String className = CORE_ROOT_PACKAGE + ".foo.CorePage";

        try
        {
            resolver.resolvePageClassNameToPageName(className);
            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Unable to resolve class name " + className + " to a logical page name.");
        }

        verify();
    }

    @Test
    public void resolver_may_provide_library_names()
    {
        String secondaryLibPackage = "org.examples.addon.lib";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();


        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                new LibraryMapping(LIB_PREFIX, secondaryLibPackage), new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertListsEquals(resolver.getLibraryNames(), CORE_PREFIX, LIB_PREFIX);

        verify();
    }

    /**
     * The logic for searching is pretty much identical for both components and pages, so even a cursory test of
     * component types should nail it.
     */
    @Test
    public void simple_component_type()
    {
        String className = APP_ROOT_PACKAGE + ".components.SimpleComponent";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".components", className);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolveComponentTypeToClassName("SimpleComponent"), className);

        verify();
    }

    /**
     * Likewise for mixins; it's all just setup for a particular method.
     */

    @Test
    public void simple_mixin_type()
    {
        String expectedClassName = APP_ROOT_PACKAGE + ".mixins.SimpleMixin";

        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".mixins", expectedClassName);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolveMixinTypeToClassName("SimpleMixin"), expectedClassName);

        verify();
    }

    @Test
    public void mixin_type_not_found()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.resolveMixinTypeToClassName("SimpleMixin");
            unreachable();
        } catch (UnknownValueException ex)
        {
            assertMessageContains(ex, "Unable to resolve 'SimpleMixin' to a mixin class name.");
        }

        verify();
    }

    @Test
    public void component_type_not_found()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        replay();

        ComponentClassResolver resolver = create(logger, locator, new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.resolveComponentTypeToClassName("SimpleComponent");
            unreachable();
        } catch (UnknownValueException ex)
        {
            assertTrue(ex.getMessage().contains("Unable to resolve 'SimpleComponent' to a component class name."));
        }

        verify();
    }

    @Test
    public void common_package_name()
    {
        List<String> packageNames = CollectionFactory.newList("org.example.app.main", "org.example.app.sub");

        assertEquals(ComponentClassResolverImpl.findCommonPackageName(packageNames), "org.example.app");
    }

    @Test
    public void common_package_name_for_single_package()
    {
        List<String> packageNames = CollectionFactory.newList("org.example.app.main");

        assertEquals(ComponentClassResolverImpl.findCommonPackageName(packageNames), "org.example.app.main");
    }

    @Test
    public void expect_failure_when_no_common_package()
    {
        List<String> packageNames = CollectionFactory.newList("org.example.app.main", "demo.app.sub");

        // "org" isn't good enough, we expect at least two terms.

        try
        {

            ComponentClassResolverImpl.findCommonPackageNameForFolder("fred", packageNames);

            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Package names for library folder 'fred' (demo.app.sub, org.example.app.main) can not be reduced to a common base package (of at least one term).");
        }

    }

    @Test
    public void ignore_start_page_outside_root()
    {
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        String[] classNames = new String[]
                {APP_ROOT_PACKAGE + ".pages.exam.ExamIndex", APP_ROOT_PACKAGE + ".pages.exam.StartExam"};

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", classNames);

        replay();

        ComponentClassResolver resolver = create(logger, locator);

        assertEquals(resolver.resolvePageNameToClassName("exam"), classNames[0]);

        verify();
    }
}
