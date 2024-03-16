package com.start.helm.domain.helm.chart;

import com.start.helm.domain.helm.HelmContext;
import com.start.helm.domain.helm.chart.providers.HelmFileProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for generating helm charts based on input
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HelmChartService {

	private final List<HelmFileProvider> providers;

	/**
	 * Method for turning a populated {@link HelmContext} into a zip file containing a
	 * Helm Chart. The zip file is returned as a byte array.
	 */
	@SneakyThrows
	public byte[] process(HelmContext context) {
		return this.process(context, new ByteArrayOutputStream(), false);
	}

	@SneakyThrows
	public byte[] process(HelmContext context, ByteArrayOutputStream outputStream, boolean addParentDirectory) {
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

		zipOutputStream.putNextEntry(new ZipEntry("templates/"));
		zipOutputStream.closeEntry();

		providers.forEach(p -> {
			String fileContent = p.getFileContent(context);
			String fileName = p.getFileName();
			addZipEntry(fileName, fileContent, zipOutputStream);
		});

		context.getHelmChartSlices()
			.stream()
			.filter(s -> Objects.nonNull(s.getExtraSecrets()))
			.filter(s -> !s.getExtraSecrets().isEmpty())
			.flatMap(s -> s.getExtraSecrets().stream())
			.forEach(extraSecret -> {
				String fileContent = extraSecret.getYaml();
				String fileName = "templates/" + extraSecret.getFileName();
				log.info("Adding extra secret {}", fileName);
				addZipEntry(fileName, fileContent, zipOutputStream);
			});

		context.getHelmChartSlices()
			.stream()
			.filter(s -> Objects.nonNull(s.getExtraFiles()))
			.filter(s -> !s.getExtraFiles().isEmpty())
			.flatMap(s -> s.getExtraFiles().stream())
			.forEach(extraFile -> {
				String fileContent = extraFile.getContent();
				String fileName = "templates/" + extraFile.getFileName();
				log.info("Adding extra file {}", fileName);
				addZipEntry(fileName, fileContent, zipOutputStream);
			});

		zipOutputStream.close();
		outputStream.close();

		return outputStream.toByteArray();
	}

	private void addZipEntry(String filename, String content, ZipOutputStream zipOutputStream) {
		ZipEntry zipEntry = new ZipEntry(filename);
		try {
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(content.getBytes());
			zipOutputStream.closeEntry();
		}
		catch (Exception e) {
			log.error("Error while processing file: {}", filename, e);
		}
	}

}
