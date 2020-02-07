    
    # Amazon Search Volume Calculation   
    
      
        ## General idea
        
            The idea is the when we are typing in the search box of the Amazon, the more we type for the sub-prefixes,
            the more the word become apparent in the search results. For the words, that are very demanding or hot, 
            as soon as we type the first letter, we may be able to see the keyword inside the search results which suppress
            the other results starts with the same keyword.    
            
            As it turns out iphone charger is a really popular term and scores great with this approach.  
            Lets check: "iphone charger"  
            
        
                ```bash  
                     
                     iphone cahrger -> hit  
                     iphone cahrge  -> hit  
                     iphone cahrg   -> hit  
                     iphone cahr    -> hit   
                     iphone cah     -> hit  
                     iphone ca      -> hit  
                     iphone c       -> hit  
                     iphone         -> hit  
                     iphon          -> hit  
                     ipho           -> miss  
                    
                ```  
        
            We can see that until we type the word `ipho`, we are able to see the full search term in the Amazon search box.  
          
          
        ## b) The algorithm
          
            As discussed above, a search term hotness would be measured by how many of that search terms prefixes still result in the search term as an autocomplete suggestion.  
            A couple minor details:  
            
                a. We need to normalize by word length. The score should be number of occurrences in search results over total number of sub-prefixes, so different word lengths would fit on the same fuzzy scale.  
                b. Not all search results are equal. If there are less then 10 results from a query its a good sign that we are into a very specific space of suggestions instead of a hot space of suggestions. We can modify the "counting" of the point above and add "weight" to each count  
                c. Not all word endings are important. Trailing spaces are almost always meaningless. If we go deeper, we can find a lot more language specific "stop words", but for now, we need only consider them as a configurable list. Just spaces will suffice for now.  
                d. We need a iterative approach. At any given time we can interrupt the process.  
              
            The algorithm in pseudo-code:  
                
                ```bash
                  
                score=0;  
                Foreach subprefix of originalQuery:  
                    
                    results = apicall(subprefix); 
                    
                    if(results.contains(originalQuery))
                        score += calculateIterationWeigth(results)  
                    else  
                        break;  

                    if(timeOut())  
                        break;    
                    
                return score/originalQuery.length  
                ```  
    
       
        ## Building the project  
                  
                The project can be built by running:
                  
                    $ mvn clean install dockerfile:build 
                   
                This produces a .jar file and a docker image.  
              
                ## Running the project  
                  
                  If building of the project completed successfully, the project can be run either from the .jar directly:  
                   
                    $ java -jar target/amazonSearchVolume-1.0-SNAPSHOT.jar  
                 
                  or via docker:
                   
                    $ docker run -it -p 8080:8080  