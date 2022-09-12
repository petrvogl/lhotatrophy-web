package cz.lhotatrophy.core.service;

import cz.lhotatrophy.persist.dao.TaskDao;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Petr Vogl
 */
@Service
@Log4j2
public class TaskServiceImpl extends AbstractService implements TaskService {

	@Autowired
	private transient TaskDao taskDao;

}
