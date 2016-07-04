import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.web.client.RestTemplate

import com.github.shredder121.gh_event_api.GHEventApiServer
import com.github.shredder121.gh_event_api.handler.issues.IssuesHandler
import com.github.shredder121.gh_event_api.handler.issues.IssuesPayload
import com.github.shredder121.gh_event_api.handler.pull_request.PullRequestHandler
import com.github.shredder121.gh_event_api.handler.pull_request.PullRequestPayload

/**
 * Webhook implementation based on the Spring Issuemaster behavior.
 *
 * @author Shredder121
 */
class Application {

	@Autowired RestTemplate restTemplate

	@Bean PullRequestHandler pullRequestHandler() {
		def bean = { PullRequestPayload payload ->
			if (payload.action == 'opened') {
				def pr = payload.pullRequest
				handleOpenedIssueOrPr(pr.links['issue'].href)
			}
		}
	}

	@Bean IssuesHandler issuesHandler() {
		def bean = { IssuesPayload payload ->
			if (payload.action == 'opened') {
				def issue = payload.issue
				handleOpenedIssueOrPr(issue.url)
			}
		}
	}

    @Bean RestTemplate restTemplate(Environment env) {
		def user = env.getRequiredProperty('github_user')
		def oauth = env.getRequiredProperty('github_oauth')
		def bean = new TestRestTemplate(user, oauth)
    }

	def handleOpenedIssueOrPr(issueLink) {
//		sleeping prevents the label from showing up too quickly for GitHub
		Thread.sleep 5_000
		restTemplate.postForObject("$issueLink/labels", ['waiting-for-triage'], Object)
	}
}

GHEventApiServer.start Application
