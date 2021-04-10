#!/usr/bin/groovy

import groovy.json.JsonSlurperClassic


/**
 * waitForTestingFarm() step.
 */
def call(Map params = [:]) {
    def requestId = params.get('requestId')
    def hook = params.get('hook')

    def tfArtifactsBaseUrl = env.FEDORA_CI_TESTING_FARM_ARTIFACTS_URL
    def apiUrl = env.FEDORA_CI_TESTING_FARM_API_URL

    echo "**********************************************************************************************************************"
    echo "Testing Farm API Request URL: ${apiUrl}/v0.1/requests/${requestId}"
    echo "Testing Farm Artifacts URL: ${tfArtifactsBaseUrl}/${requestId}"
    echo "\n"

    echo "Waiting for Testing Farm..."
    def statusResult
    while (true) {
        data = waitForWebhook(hook)
        statusResult = checkTestingFarmRequestStatus(requestId)
        echo "The status is now \"${statusResult.status}\""
        if (statusResult.status in ['complete', 'error']) {
            break
        }
        echo "Waiting for Testing Farm..."
    }
    testingFarmResult = statusResult.response
    xunit = testingFarmResult.get('result', [:])?.get('xunit', '') ?: ''

    return [apiResponse: statusResult.response, requestStatus: statusResult.status, xunit: xunit]
}


def checkTestingFarmRequestStatus(requestId) {

    def apiUrl = env.FEDORA_CI_TESTING_FARM_API_URL

    if (!apiUrl) {
        error('FAIL: Testing Farm API URL is not configured')
    }

    apiUrl = apiUrl + '/v0.1/requests/' + "${requestId}"

    def response
    def contentJson

    retry(30) {
        try {
            response = httpRequest(
                consoleLogResponseBody: false,
                contentType: 'APPLICATION_JSON',
                httpMode: 'GET',
                url: "${apiUrl}",
                validResponseCodes: '200'
            )
            contentJson = new JsonSlurperClassic().parseText(response.content)
            return [status: contentJson.get('state'), response: contentJson]
        } catch(e) {
            sleep(time: 10, unit: "SECONDS")
            error("Failed to call Testing Farm: ${e.getClass().getCanonicalName()}: ${e.getMessage()}")
        }
    }
}
