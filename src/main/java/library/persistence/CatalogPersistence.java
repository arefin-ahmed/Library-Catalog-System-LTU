package library.persistence;

import java.util.List;
import java.util.Map;
import library.models.Book;

public interface CatalogPersistence {
    void saveCatalog(Map<String, List<Book>> catalog) throws Exception;   //write the file

    Map<String, List<Book>> loadCatalog() throws Exception;          //read from file
}