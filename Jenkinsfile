#!groovy

node {

    try {
        stage 'Checkout'
            checkout scm

        stage 'Test'
            sh """
            mvn test
            """

        stage 'Build'
            
    }

    catch (err) {
        
        throw err
    }
    
}

