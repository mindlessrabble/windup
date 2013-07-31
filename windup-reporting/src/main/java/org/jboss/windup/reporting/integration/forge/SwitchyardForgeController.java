package org.jboss.windup.reporting.integration.forge;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.facets.Facet;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.Faceted;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourceFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.manager.AddonManager;
import org.jboss.forge.furnace.manager.impl.AddonManagerImpl;
import org.jboss.forge.furnace.manager.maven.addon.MavenAddonDependencyResolver;
import org.jboss.forge.furnace.manager.request.InstallRequest;
import org.jboss.forge.furnace.manager.spi.AddonDependencyResolver;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.hint.Hint;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

public class SwitchyardForgeController implements Reporter
{
	

   private static final Log LOG = LogFactory.getLog(SwitchyardForgeController.class);
   {
      System.setProperty("modules.ignore.jdk.factory", "true");
   }

   @Override
   @SuppressWarnings("unchecked")
   public void process(ArchiveMetadata archive, File reportDirectory)
   {
//	   this.examineArchive(archive, 0);
      File forgeOutput = new File(reportDirectory, "forge");
      try
      {
         final Furnace furnace = FurnaceFactory.getInstance();
         try
         {
            FileUtils.forceMkdir(forgeOutput);

            furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(OperatingSystemUtils.getUserHomeDir(),
                     ".windup"));
            furnace.startAsync();

            while (!furnace.getStatus().isStarted())
            {
               LOG.info("FURNACE STATUS: " + furnace.getStatus());
               Thread.sleep(100);
            }

            install(furnace, "org.jboss.forge.addon:parser-java,2.0.0.Alpha8");
            install(furnace, "org.jboss.forge.addon:projects,2.0.0.Alpha8");
            install(furnace, "org.jboss.forge.addon:maven,2.0.0.Alpha8");
            install(furnace, "org.switchyard.forge:switchyard-forge-plugin,1.0.0-SNAPSHOT");


            AddonRegistry registry = furnace.getAddonRegistry();
            Addons.waitUntilStarted(registry.getAddon(AddonId.from("org.switchyard.forge:switchyard-forge-plugin", "1.0.0-SNAPSHOT")));
            Addons.waitUntilStarted(registry.getAddon(AddonId.from("org.jboss.forge.addon:projects", "2.0.0.Alpha8")));

            ResourceFactory resourceFactory = registry.getExportedInstance(ResourceFactory.class).get();
            ProjectFactory projectFactory = registry.getExportedInstance(ProjectFactory.class).get();
            FacetFactory facetFactory = registry.getExportedInstance(FacetFactory.class).get();

            
            DirectoryResource dr = resourceFactory.create(DirectoryResource.class, forgeOutput);
            DirectoryResource projectDir = dr.getChildDirectory(archive.getName());
            projectDir.mkdir();

            List<Class<? extends ProjectFacet>> facetsToInstall = Arrays.asList(JavaSourceFacet.class,
                     ResourceFacet.class);
            Project project = projectFactory.createProject(projectDir, facetsToInstall);

            if (project != null){
           
               LOG.info("Project created: " + project);
               project.getFacet(JavaSourceFacet.class).saveJavaSource(
                        JavaParser.create(JavaClass.class).setPackage("com.example").setName("ExampleClass"));
               MetadataFacet mdf = project.getFacet(MetadataFacet.class);
               mdf.setProjectName(archive.getName());
               
//               SwitchYardFacet installed = facetFactory.install(project, SwitchYardFacet.class);
               
            

               
               Facet facet = facetFactory.install(project, SwitchYardFacet.class);
               LOG.info("***** Proxy.unwrapClass" + Proxies.unwrap(facet).getClass());
               LOG.info("***** Proxy.unwrap" + Proxies.unwrap(facet));

               Faceted faceted = facet.getFaceted();
              
               if(facet.install()){
            	   LOG.info("Facet.install() returns true"); 
               }else{
            	   LOG.info("Facet.install() returns false ");
               }
               if(facet.isInstalled()){
            	   LOG.info("facet.isInstalled() return true");
               }
               for(Facet facet2 : project.getFacets()){
            	   LOG.info("Facets found " +facet2.getFaceted());
               }
               if(project.hasFacet(SwitchYardFacet.class)){
            	   LOG.info("PROJECT HAS FACET SwitchYardFacet!!!!!!!!!");
               }else{
            	   LOG.info("PROJECT Does NOT have FACET SwitchYardFacet!!!!!");
               }

               
               
               if(SwitchYardFacet.class.isInstance(facet)){
            	   LOG.info("FACET is instanceOf SwitchyardFacet");
               }else{
            	   LOG.info("Facet is NOT instanceOf SwtichYardFact");
               }
               
               try{
            	   SwitchYardFacet syf = (SwitchYardFacet)facetFactory.install(project, SwitchYardFacet.class);
               }catch(Exception ex){
            	   ex.printStackTrace();
               }
               
//               syf.install();
//               
//               CompositeReferenceModel crm = syf.getCompositeReference(archive.getName());
//               
//               if(crm==null){
//            	   LOG.info("CompositeReference is still NULL after syf.install ");
//               }else{
//            	   LOG.info("CompositeReference is NOT null after syf.install and name is " + crm.getName());
//               }
               
              
            }

         }
         finally
         {
            furnace.stop();
            LOG.info("Furnace stopped.");
         }

      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
   }
   

   private void examineArchive(ArchiveMetadata archive, int level) {
	String arvhiveName = archive.getName();
	LOG.info("Archive Name = "+ archive.getName());	
	
	for (FileMetadata entry : archive.getEntries()) {
		
		LOG.info(" is Instance of   " +entry.getClass().getName());
			this.examineSubEntries(entry, level);
		
	}
	
	for(AbstractDecoration ad :archive.getDecorations()){
		this.examineDecorations(ad, level);
	}
	
	LOG.info("NUmber of Nested Archives " + archive.getNestedArchives().size());
	
	for(ArchiveMetadata am : archive.getNestedArchives()){
		LOG.info("Nested ArchiveNaem " +am.getName());
		this.examineArchive(am, level);
	}
		
}
   
private void examineSubEntries(FileMetadata entry, int level){
	level++;
	  StringBuilder sb = new StringBuilder();
	for(int i=0;i<=level;i++){
		sb.append("+");
	}
	   
	LOG.info("Entry  file =  " + sb.toString() + entry.getFilePointer().getName());
	if(!entry.getDecorations().isEmpty()){
		for(AbstractDecoration ad : entry.getDecorations()){
			this.examineDecorations(ad, level);
		}
	}
}

private void examineDecorations(AbstractDecoration ad, int level){
	level++;
	  StringBuilder sb = new StringBuilder();
	for(int i=0;i<=level;i++){
		sb.append("+");
	}
	LOG.info(" Decoration Description       " + sb.toString() + ad.getDescription());
	
	for(Hint hint: ad.getHints()){
		this.examineHints(hint);
	}
}

private void examineHints(Hint hint) {
	
}


   private void install(Furnace furnace, String addonCoordinates)
   {
      try
      {
         AddonDependencyResolver addonResolver = new MavenAddonDependencyResolver();
         AddonManager addonManager = new AddonManagerImpl(furnace, addonResolver, false);

         AddonId addon;
         // This allows forge --install maven
         if (addonCoordinates.contains(","))
         {
            addon = AddonId.fromCoordinates(addonCoordinates);
         }
         else
         {
            String coordinates = "org.jboss.forge.addon:" + addonCoordinates;
            CoordinateBuilder coordinate = CoordinateBuilder.create(coordinates);
            AddonId[] versions = addonResolver.resolveVersions(coordinate.toString());
            if (versions.length < 1)
            {
               throw new IllegalArgumentException("No Artifact version found for " + coordinate);
            }
            addon = versions[versions.length - 1];
         }

         InstallRequest request = addonManager.install(addon);
         System.out.println(request);
         request.perform();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
