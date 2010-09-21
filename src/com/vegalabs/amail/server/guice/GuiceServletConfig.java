package com.vegalabs.amail.server.guice;

import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.vegalabs.general.server.command.CommandFetcher;
import com.vegalabs.general.server.rpc.JsonRpcProcessor;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.servlet.MailHandlerServlet;
import com.vegalabs.amail.server.servlet.RegisterRobotServlet;
import com.vegalabs.amail.server.admin.CommandFetcherImpl;
import com.vegalabs.amail.server.dao.EmailEventDao;
import com.vegalabs.amail.server.dao.EmailEventDaoImpl;
import com.vegalabs.amail.server.dao.EmailThreadDao;
import com.vegalabs.amail.server.dao.EmailThreadDaoImpl;
import com.vegalabs.amail.server.dao.PersonDao;
import com.vegalabs.amail.server.dao.PersonDaoImpl;
import com.vegalabs.amail.server.dao.TokenDao;
import com.vegalabs.amail.server.dao.TokenDaoImpl;

public class GuiceServletConfig extends GuiceServletContextListener {
  private static final Logger LOG = Logger.getLogger(GuiceServletConfig.class.getName());

  @Override
  protected Injector getInjector() {
    ServletModule servletModule = new ServletModule() {
      @Override
      protected void configureServlets() {
        serveRegex("\\/_wave/.*").with(WaveMailRobot.class);
        serve("/admin/jsonrpc").with(JsonRpcProcessor.class);
        serveRegex("\\/_ah/mail/.*").with(MailHandlerServlet.class) ;
        serve("/LoginServlet").with(com.vegalabs.amail.server.authSub.LoginServlet.class) ; 
  	  serve("/HandleTokenServlet").with(com.vegalabs.amail.server.authSub.HandleTokenServlet.class); 
  	  serve("/SuccessMessageServlet").with(com.vegalabs.amail.server.authSub.SuccessMessageServlet.class); 
        
        serve("/_wave/verify_token").with(RegisterRobotServlet.class);
        

//        serve("/send_email_updates").with(SendEmailUpdates.class);
//        serve("/send_wave_updates").with(SendWaveUpdates.class);
//        serve("/add_participants_task").with(AddParticipantsTask.class);
//        
//        serve("/feeds/get_tag_counts").with(GetTagCounts.class);        
//        serve("/feeds/get_post_counts").with(GetPostCounts.class);
//        serve("/feeds/get_forum_posts").with(GetForumPosts.class);
//        serve("/feeds/get_latest_digest").with(GetLatestDigest.class);
//        serve("/feeds/json").with(JsonGenerator.class);
//        serve("/feeds/atom").with(AtomGenerator.class);       
//        serve("/installNew").with(InstallServlet.class); 
//        serve("/installGadget").with(InstallGadgetServlet.class); 
//        serve("/serveAd").with(ServeAdGadgetServlet.class); 
//        serve("/gadgetRPC").with(GadgetRPCServlet.class); 
//        serve("/embed").with(ServeEmbedServlet.class); 
//        serve("/serveAdInstaller").with(ServeAdInstallerServlet.class);
        
        
        
        
        
        
//        serve("/info").with(InfoServlet.class);  //TODO remove
//        serve("/info").with(InstallServlet.class);  //TODO remove
        
        //serve("/migrateTags").with(MigrateTags.class);
        //serve("/migrateTagsTask").with(MigrateTagsTask.class);
      }
    };

    AbstractModule businessModule = new AbstractModule() {
      @Override
      protected void configure() {
//        bind(ForumPostDao.class).to(ForumPostDaoImpl.class);
//        bind(TagDao.class).to(TagDaoImpl.class);
//        bind(UserNotificationDao.class).to(UserNotificationDaoImpl.class);
//        bind(AdminConfigDao.class).to(AdminConfigDaoImpl.class);
//        bind(DigestDao.class).to(ExtDigestDaoImpl.class);
//        bind(ExtDigestDao.class).to(ExtDigestDaoImpl.class);
//        bind(BlipSubmitedDao.class).to(BlipSubmitedDaoImpl.class);
//        bind(InfluenceDao.class).to(InfluenceDaoImpl.class);
        bind(PersonDao.class).to(PersonDaoImpl.class);
        bind(EmailEventDao.class).to(EmailEventDaoImpl.class);
        bind(CommandFetcher.class).to(CommandFetcherImpl.class);
        bind(EmailThreadDao.class).to(EmailThreadDaoImpl.class);
        bind(TokenDao.class).to(TokenDaoImpl.class);
//        bind(TrackerEventDao.class).to(TrackerEventDaoImpl.class);
//        bind(AdEventDao.class).to(AdEventDaoImpl.class);
      }

      @Provides
      @Singleton
      PersistenceManagerFactory getPmf() {
        return JDOHelper.getPersistenceManagerFactory("transactions-optional");
      }
    };

    return Guice.createInjector(servletModule, businessModule);
  }
}
