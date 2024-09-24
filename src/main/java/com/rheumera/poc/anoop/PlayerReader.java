package com.rheumera.poc.anoop;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rheumera.poc.anoop.beans.LineItem;
import com.rheumera.poc.anoop.beans.Player;
import com.rheumera.poc.anoop.flatfile.HeaderProcessor;
import com.rheumera.poc.anoop.flatfile.JsonFieldSetMapper;
import com.rheumera.poc.anoop.flatfile.RhDelimitedLineMapper;

@Component
public class PlayerReader {
	public void read() throws Exception {

		FlatFileItemReader<LineItem<Player>> itemReader = new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource("data/players.csv"));
		HeaderProcessor headerProcessor = new FileHeaderProcessor();
		RhDelimitedLineMapper<Player> lineMapper = new RhDelimitedLineMapper<>();
		lineMapper.setHeaderProcessor(headerProcessor);

		ObjectMapper mapper = new ObjectMapper();
		JsonFieldSetMapper<Player> fieldSetMapper = new JsonFieldSetMapper<Player>(Player.class, mapper);
		lineMapper.setFieldSetMapper(fieldSetMapper);

		itemReader.setLineMapper(lineMapper);
		itemReader.open(new ExecutionContext());
		LineItem<Player> lineItem = null;
		while ((lineItem = itemReader.read()) != null) {
			System.out.println(lineItem);
		}
	}
}
