/*
 * Example from https://docs.jboss.org/hibernate/stable/search/reference/en-US/html_single/#getting-started-analysis
 */
package se.backede.generics.persistence.search;

import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

/**
 *
 * @author Joakim Backede
 */
public class NegodLuceneAnalysisConfigurer implements LuceneAnalysisConfigurer {

    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {
        context.analyzer("generic").custom()
                .tokenizer("standard")
                .tokenFilter("lowercase")
                .tokenFilter("asciiFolding");
    }

}
