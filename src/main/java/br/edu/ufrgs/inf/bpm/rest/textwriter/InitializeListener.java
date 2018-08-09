package br.edu.ufrgs.inf.bpm.rest.textwriter;

import br.edu.ufrgs.inf.bpm.wrapper.WordNetWrapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class InitializeListener implements ServletContextListener {

    @Override
    public final void contextInitialized(final ServletContextEvent sce) {
        WordNetWrapper.generateDictionary();
    }

    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {

    }

}