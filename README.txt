GUI user Interface Instructions:

General Navigation Button:(from most right to most left)
- close window button
- log out button
- go to dash board home page button
- go back to previous page button

Admin User:

1. register admin user account: need admin user employee Id(a number) and password

2. log in admin user account: need admin user employee Id and password

3. create new route:
    - go to route management page
    - click on add new route button
    - enter station/stop name in text field
    - click add station/stop button
    - click on any grid on the grid pane (except the origin (0, 0) point) to set the location of this station/stop
      notice: one grid is a unit of distance
      SubwayStrategy calculate fare by units, distance between two stations can be different,
      However,  admin user can also create station in adjacent grid so distance between two station is also one unit of distance.
    - notice that a route must have at least two stations/stops
    - select transit type of this route, and whether to create both go and back direction together
    - enter route name in text field
    - click add route button
    - if there is an intersection of two routes, the user must add this station on both route and use exactly the same name

4. delete route: click delete button on corresponding line of that route on route management page

5. edit policy:
    - admin user can set one time fare of bus strategy, per station fare of subway strategy, reach cap time(calculated in millis),
      initial balance of new cards, add/remove fare strategy
    - go to policy edit page, enter number and click corresponding button

6. change profile picture & change password:
    - go to admin info page

7, view statistics: (on dash board page)
    - view total fare collected, total stations covered, total distance travelled of this day/week/month
    - view passenger flow of each station on this day/week/month
    - view revenue composition pie chart, how much is contributed by bus/subway



Cardholder:

1. register:
    - need cardholder email, name and password
    - name can be changed later, while email cannot be changed
2. log in:
    - enter cardholder email and password

3. create new card:
    - click on Cards button on ride hand side tools bar
    - click add card button

4. remove card:
    - click on Cards button on ride hand side tools bar
    - click on delete button on corresponding line of card

5. view card balance:
    - click on Cards button on ride hand side tools bar
    - user can view balance on this cards pane or click on details button to view more information

6. see status of this card:
    - click on Cards button on ride hand side tools bar
    - click detail button on corresponding line of card

7. see ride records of this card:
    - click on Cards button on ride hand side tools bar
    - click detail button on corresponding line of card
    - the user can select from the choice box that to  view ride record for this  day/week/month
      or just view recent 3 or view all ride records

8. suspend/reactive card/transfer balance:
     - click on Cards button on ride hand side tools bar
     - click detail button on corresponding line of card
     - a suspended card can be reactivated or transfer the balance to a new card,
        after the balance of suspended card is transfered to new card, this card will be removed automatically

9. change user name:
    - click on user info button on ride hand side tools bar
    - click on edit button
    - enter new user name
    - click save button

10. go transit:
    - click on go transit button on ride hand side tools bar
    - select a transit type and a card to use
    - the station name can be viewed from the route graph if click on station/stop button or make mouse stay on station/stop
    - click on station/stop button to select the station/stop to tap in/out
    - click on tap in/out button

11. see passenger flow of each station/stop on current day
    - the left hand side passenger flow line chart display tap in times/tap out times/arrived times of each station/stop on current day

12. see user fare report and cost composition(bus vs. subway) on dash board home page
    - user can select card to report and report period(recent 8 rides or report for this day or this week or this month)


About Fare Calculation:

1. if there are two path between cardholder tap in station/stop and tap out station/stop,
    we record the shortest path and use this shortest path to calculate fare for this ride.

2. if there is a missed tap in/out information, we consider the cardholder went to the farthest end of that route,
    and use this farthest path to calculate fare for this ride

3. Missed Tap In Case:
    - Bus: we calculate regular one time fare when the cardholder tap out but
    - Subway: we consider this case as a system error and charge $0, because subway does not charge when tap in,
      there is no normal reason for a cardholder to intend to tap out without tap in.

4. Missed Tap Out Case:
    - Bus: we don't charge extra fare since bus ride fare is charged when tap in,
      but we consider this cardholder went to farthest end of this route when calculate distance travelled, stations covered and transit path.
    - Subway: a missed tap out will be detected when next time tap in, our system will consider user went to farthest end
      of this route and calculate fare in the most recent tap in.

 5. Reach Cap:
    - currently we charge $0 if there is a continuous ride within 2 hour and fare collected for this ride reached $6.
