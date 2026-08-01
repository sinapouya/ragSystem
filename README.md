How to Run the Project


Prerequisites
Java 25 
Maven
Docker & Docker Compose
Ollama installed and running locally
(Install: curl -fsSL https://ollama.com/install.sh | sh)

1. Start PostgreSQL + pgvector
bash
docker-compose up -d
This starts a PostgreSQL container with the pgvector extension already enabled.
Default credentials: postgres / secret, database vectordb.

2. Start Ollama and Pull Required Models
Make sure Ollama is running:
in ollama folder :

docker compose up -d

docker exec -it ollama-server ollama pull llama3        # chat model (or a smaller one like llama3.2:1b)
docker exec -it ollama-server ollama pull nomic-embed-text  # embedding model
Note on model size: Smaller models (e.g., llama3.2:1b) are faster but may need exact wording. Larger models (llama3:3b, llama3) can handle rephrased or inference questions better.

3. Start the Database with Docker Compose
in vector db folder run this code : 
docker compose up -d
This single command starts PostgreSQL  with the pgvector extension pre‑installed

PostgreSQL is available at localhost:5432

4. Run the Spring Boot Application

The app will start on http://localhost:8080.

📡 Testing with the HTTP Client
The file api_ingest.http contains predefined requests you can run with an HTTP client (like IntelliJ IDEA’s built‑in client, or Visual Studio Code’s REST Client extension).

Step 1 – Ingest Plain Text Documents
Execute the first two POST requests in the file (the ones with text and source fields).
These add company policies to the vector database.

Step 2 – Ask a Question
Send the POST request to /api/query/ask:

json
{
  "question": "Can I return a broken laptop?"
}
You should get a response with the answer and, if configured, source documents.

Step 3 – Ingest a PDF File
The HTTP file includes a multipart request that uploads a PDF directly.
Place your PDF files in src/main/resources/static/ (or adjust the file path in the request).
Then run the POST /api/ingest/pdf request.

Step 4 – Test Different Query Types
The HTTP file includes several example questions that demonstrate the model’s capabilities:

Direct match – “Are digital goods refundable?” – works with small models (1b).

Slight rephrase – “Can I get my money back for a software download?” – needs a model that understands rephrasing (3b or larger).

Inference required – “Can I return a broken laptop?” – requires the model to infer that “broken” ≈ “damaged”. Larger models handle this better.

If you change the chat model in application.yml (e.g., from llama3.2:1b to llama3:3b), restart the app and re‑run the queries to see the difference.
