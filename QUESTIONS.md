# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
I would refactor panache entity for Store and move to panache repository. I think panache entities are good for simple case. Also testing rollback it's not easy. 
I tested my solution altering store sequence and generate an already used id. With my solution the legacy store manager gateway is not called, instead it's called with original code. Unit testind has been hard 
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
I think annotate in the Java classes it is more easy and intuitive, but create first an Open API yaml file, it has some benefits:
 * Design-First
 * Code geneation
 * Documentation generation
 * Test generation
 * Share API contract with heterogeneous tools, like ngnix, frontenders, etc. 
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Of course high coverage it is important but as you suggest we need to find a trade-off, with time constraints. 
During this exercise, I put priorities on unit test for use cases where there is the business logic. The rest api tests are quite simple, for example I checked only one validation error for creating an replacing.
```