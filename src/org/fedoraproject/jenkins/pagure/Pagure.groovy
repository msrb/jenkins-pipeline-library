package org.fedoraproject.jenkins.pagure

import groovy.json.JsonSlurperClassic


/*
 * A collection of helper methods for Pagure.
 */
class Pagure implements Serializable {

    URL apiUrl

    Pagure() {
        this(System.getenv('FEDORA_CI_PAGURE_DIST_GIT_API_URL') ?: 'https://src.fedoraproject.org/api/0')
    }

    /*
     * Constructor.
     *
     * @param url Pagure API URL
     */
    Pagure(String apiUrl) {
        this.apiUrl = apiUrl.toURL()
    }

    def getPullRequestInfo(String pullRequestId) {
        def pullRequestId = params.get('pullRequestId')

        def uid = splitPullRequestId(pullRequestId)[0]

        def url = this.apiUrl + '/pull-requests/' + uid

        // https://src.fedoraproject.org/api/0/pull-requests/
        def response = httpRequest consoleLogResponseBody: false, contentType: 'APPLICATION_JSON', httpMode: 'GET', url: "${url}", validResponseCodes: '200'
        def contentJson = new JsonSlurperClassic().parseText(response.content)
        return contentJson
    }

    /*
     * Split given Pull Request Id into individual components — uid, commit id, comment id
     *
     * @param pullRequestId Pull Request Id
     * @return an array with 3 elements: uid, commit id, comment id
     */
    def splitPullRequestId(String pullRequestId) {
        def uid, commitAndComment = pullRequestId.split('@')
        def commit, comment = commitAndComment.split('#')

        return [uid, commit, comment]
    }
