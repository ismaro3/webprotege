package edu.stanford.bmir.protege.web.server.download;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.server.session.WebProtegeSession;
import edu.stanford.bmir.protege.web.server.session.WebProtegeSessionImpl;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static edu.stanford.bmir.protege.web.server.logging.RequestFormatter.formatAddr;

/**
 * Author: Ismael Rodriguez<br>

 * <p>
 * A servlet which return the latest version number of a certain project.
 * </p>
 */
public class ProjectVersionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProjectVersionServlet.class);

    @Nonnull
    private final AccessManager accessManager;

    @Nonnull
    private final ProjectDownloadService projectDownloadService;



    @Inject
    public ProjectVersionServlet(@Nonnull AccessManager accessManager,
                                 @Nonnull ProjectDownloadService projectDownloadService) {
        this.accessManager = accessManager;
        this.projectDownloadService = projectDownloadService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final WebProtegeSession webProtegeSession = new WebProtegeSessionImpl(req.getSession());
        UserId userId = webProtegeSession.getUserInSession();
        FileDownloadParameters downloadParameters = new FileDownloadParameters(req);
        if(!downloadParameters.isProjectDownload()) {
            logger.info("Bad project version request from {} at {}.  Request URI: {}  Query String: {}",
                        webProtegeSession.getUserInSession(),
                        formatAddr(req),
                        req.getRequestURI(),
                        req.getQueryString());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        logger.info("Received version request from {} at {} for project {}",
                    userId,
                    formatAddr(req),
                    downloadParameters.getProjectId());


        if (downloadParameters.isProjectDownload()) {
            sendLastVersion(resp, userId, downloadParameters);
        }
    }

    private void sendLastVersion(HttpServletResponse resp,
                                      UserId userId,
                                      FileDownloadParameters downloadParameters) throws IOException {
        ProjectId projectId = downloadParameters.getProjectId();

        RevisionNumber revNumber = projectDownloadService.getRevisionNumber(projectId,userId);


        resp.getWriter().write("{\"version\":"+revNumber.getValueAsInt()+"}");
        resp.setContentType("application/json");
        resp.getWriter().close();

    }

    @Override
    public void destroy() {
        super.destroy();
        projectDownloadService.shutDown();
    }
}
