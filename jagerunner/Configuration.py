
class Configuration:
    driver = 'jageplatform'
    
    execpath = '/home/ghik/sem/IntObl/intobl/jage/algorithms/applications/emas-app'
    agexml = "classpath:age.xml"
    dotreplacer = '_'
    
    constantParameters = ['outfile', 'steps', 'problem_size', 'islands_number', 'individual_chanceToMigrate']
    
    steps = 1000
    problem_size = 10
    islands_number = 5
    individual_chanceToMigrate = 0.001
