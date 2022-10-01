package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.filestore.FileStoreEnum;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class FileStoreServiceImpl extends AbstractService implements FileStoreService {

	/**
	 * Temporary files directory path
	 */
	@Value("${lhotatrophy.filestore.temp.path}")
	private transient String tempPath;
	/**
	 * User uploaded files store path
	 */
	@Value("${lhotatrophy.filestore.user-upload.path}")
	private transient String userUploadStorePath;

	/**
	 * Directory for temporary files
	 */
	private transient File tempDir = null;
	/**
	 * Map filestore types to their root directories
	 */
	private final transient Map<FileStoreEnum, File> fileStoreDirMap = new HashMap<>();

	@Override
	public void store(
			@NonNull final FileStoreEnum fileStore,
			@NonNull final InputStream inputStream,
			@NonNull final String fileName
	) throws IOException {
		final File tempFile = createTempFile();
		try (final FileOutputStream fos = new FileOutputStream(tempFile)) {
			byte[] buff = new byte[4096];
			int cnt;
			while ((cnt = inputStream.read(buff)) != -1) {
				fos.write(buff, 0, cnt);
			}
		}
		store(fileStore, tempFile, fileName);
	}

	@Override
	public void store(
			@NonNull final FileStoreEnum fileStore,
			@NonNull final File file,
			@NonNull final String fileName
	) throws IOException {
		checkFileIsReadable(file);
		final File destFile = new File(getFileAbsolutePath(fileStore, fileName));
		final File destFileParent = destFile.getParentFile();
		if (!destFileParent.exists()) {
			if (!destFileParent.mkdirs() && !destFileParent.exists()) {
				throw new IOException("Can not create parent directories: " + destFile.getAbsolutePath());
			}
		}
		if (!file.equals(destFile)) {
			if (!file.renameTo(destFile)) {
				throw new IOException("Can not move file from [" + file.getAbsolutePath() + "] to [" + destFile.getAbsolutePath() + "].");
			}
		} else {
			log.warn("Try to rewrite existing file: {}", file.getPath());
		}
	}

	@Override
	public Optional<File> load(@NonNull final FileStoreEnum fileStore, @NonNull final String fileName) {
		final File file = new File(getFileAbsolutePath(fileStore, fileName));
		if (!file.isFile() || !file.exists()) {
			return Optional.empty();
		}
		return Optional.of(file);
	}

	/**
	 * Returns temporary file.
	 */
	private File createTempFile() throws IOException {
		final File tempFile = File.createTempFile(getClass().getSimpleName(), null, getTempDir());
		if (!tempFile.canRead()) {
			throw new IllegalStateException("Can not read from temporary file.");
		}
		if (!tempFile.canWrite()) {
			throw new IllegalStateException("Can not write to temporary file.");
		}
		return tempFile;
	}

	/**
	 * Returns directory for temporary files.
	 */
	private File getTempDir() {
		if (tempDir == null) {
			tempDir = new File(tempPath);
		}
		return tempDir;
	}

	/**
	 * Returns root directory of the specified file store.
	 */
	private File getFileStoreDir(final FileStoreEnum fileStore) {
		File storeRootDir = fileStoreDirMap.get(fileStore);
		if (storeRootDir != null) {
			return storeRootDir;
		}
		switch (fileStore) {
			case USER_UPLOAD:
				storeRootDir = new File(userUploadStorePath);
				fileStoreDirMap.put(fileStore, storeRootDir);
				break;
			default:
				throw new UnsupportedOperationException("Unsupported file store type: " + fileStore.name());
		}
		return storeRootDir;
	}

	private String getFileAbsolutePath(final FileStoreEnum fileStore, final String fileName) {
		return getFileStoreDir(fileStore).getAbsolutePath() + File.separatorChar + fileName;
	}

	private void checkFileIsReadable(final File file) throws IOException {
		if (!file.isFile()) {
			throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("Can not read file: " + file.getAbsolutePath());
		}
	}
}
