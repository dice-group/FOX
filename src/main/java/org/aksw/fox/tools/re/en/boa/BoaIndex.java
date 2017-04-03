package org.aksw.fox.tools.re.en.boa;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * <code>
    Documents size: 62728

    URIs:
    http://dbpedia.org/ontology/leaderName
    http://dbpedia.org/ontology/author
    http://dbpedia.org/ontology/starring
    http://dbpedia.org/ontology/deathPlace
    http://dbpedia.org/ontology/foundationPlace
    http://dbpedia.org/ontology/birthPlace
    http://dbpedia.org/ontology/team
    http://dbpedia.org/ontology/subsidiary
    http://dbpedia.org/ontology/award
    http://dbpedia.org/ontology/spouse
</code>
 */
abstract class BoaIndex {
  protected static Logger LOG = LogManager.getLogger(BoaIndex.class);

  protected String file = null;
  protected IndexReader indexReader = null;
  protected IndexSearcher indexSearcher = null;
  protected Directory dir = null;

  /**
   *
   * Constructor.
   *
   * @param file boa index
   */
  public BoaIndex(final String file) {
    this.file = file;

  }

  public void closeIndexSearcher() throws IOException {
    if (indexReader != null) {
      indexReader.close();
      dir.close();
    }
  }

  public IndexSearcher openIndexSearcher() throws IOException {
    closeIndexSearcher();
    dir = FSDirectory.open(new File(file));
    indexReader = DirectoryReader.open(dir);
    indexSearcher = new IndexSearcher(indexReader);
    return indexSearcher;
  }
}
