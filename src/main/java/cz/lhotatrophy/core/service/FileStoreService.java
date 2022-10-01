package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.filestore.FileStoreEnum;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.NonNull;

/**
 *
 * @author Petr Vogl
 */
public interface FileStoreService extends Service {

	/**
	 * Saves the file to the specified storage.
	 *
	 * @param fileStore File store type
	 * @param inputStream Source file data
	 * @param fileName Name of the file
	 * @throws IOException If an I/O error occurs
	 */
	void store(@NonNull FileStoreEnum fileStore, @NonNull InputStream inputStream, @NonNull String fileName) throws IOException;

	/**
	 * Moves the file to the specified storage.
	 *
	 * @param fileStore File store type
	 * @param file File
	 * @param fileName New name of the file
	 * @throws IOException If an I/O error occurs
	 */
	void store(@NonNull FileStoreEnum fileStore, @NonNull File file, @NonNull String fileName) throws IOException;

	/**
	 * Returns a file from the specified storage. If the file does not exist, it
	 * returns empty {@link Optional}.
	 *
	 * @param fileStore File store type
	 * @param fileName Requested file name
	 * @return Optional file
	 */
	Optional<File> load(@NonNull FileStoreEnum fileStore, @NonNull String fileName);
}
