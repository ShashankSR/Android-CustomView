Due to extremely limited time schedule I couldn't complete the functionality as given in the requirement.
The current implementation has a custom view to handle input as given in the requirement. I used 0 in place
of X and 1 in place of the space, as the character width didn't match as expected. It would require further
study on my part, to match the place holder and input text character widths, which would take up more time 
than i can commit to.   

The further steps / commits that needed to be done are as follows

[] Handle character spacing in the view ( Will take some time)
[] Add space after 4 digits for both placeholder and input text
[] Change background and text colors
[] Add icon on the top left
[] Add retrofit to the project
[] Add API class and API end point. The endpoint would return a Single object
[] Add response data class model 
[] Add lifecycle owner into the view
[] Dispose single onStop of lifecycle event
[] Hit api on every input
[] On Success Update the icon on top right on success
[] On Success if the input length is 16 and no valid success response is obtained show invalid card
[] On Error Ignore on API errors ( No view state given for api error ) 
[] Optimized API call frequency. Probably hit for the first 4 digits 
[] Code clean up as necessary

Please find the app-debug.apk in the root directory of the project.