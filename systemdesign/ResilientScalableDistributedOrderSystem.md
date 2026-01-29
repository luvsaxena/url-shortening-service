1. Resilience: The "Never-Fail" Strategy
   Humne handle kiya ki agar system middle state mein crash ho jaye toh kya hoga.

State Machine: Har order ek clear lifecycle se guzrega (PENDING -> SUCCESS/FAILED).

Recovery Workers: Periodic background jobs jo "Hanging States" ko scan karte hain aur upstream/downstream systems ko probe karke (API calls) unhe terminal state tak pahunchate hain.

Exponential Backoff & DLQ: Retries infinite nahi honi chahiye. Failure hone par hum wait time badhayenge aur limit hit hone par record ko Dead Letter Queue (DLQ) mein dalenge manual check ke liye.

2. Performance: Sharding & Database Strategy
   Jab data 500GB+ ho jaye, toh ek single database handle nahi kar sakta.

Partitioning: Time-based (Monthly) partitions taaki queries sirf recent data scan karein.

Sharding by User_ID: Data ko multiple physical machines par baanta taaki load distribute ho.

The Celebrity Problem (VIPs): Amazon/Zomato jaise high-volume users ke liye Dedicated Shards (Isolated VIP lane) banaye taaki normal users ka experience "Noisy Neighbors" ki wajah se kharab na ho.

3. Read/Write Optimization (CQRS)
   Humne Write (Processing) aur Read (History) ko alag kiya.

Primary Store (SQL): Transactions aur consistency ke liye.

Read Model (Elasticsearch/NoSQL): Heavy search aur historical queries ke liye.

Hybrid Fetch: Latest data (last 24-48 hours) ke liye Composite Index (user_id, created_at) ka use karke direct SQL se fetch kiya taaki Strong Consistency mile, baaki history ES se.

4. Storage Lifecycle: Data Archival
   Tiered Storage: 2 saal se purana data SQL se delete karke S3 (Cold Storage) mein move kiya.

Archive Request: User ko "Request Statement" ka option diya jo background mein S3 se data nikal kar email kar deta hai, jisse hamara "Hot DB" hamesha lean rehta hai.

5. Observability: The System's Eyes
   Humne system ko monitor karne ke liye 3 pillars set kiye:

Prometheus (Metrics): Operational health ke liye (High-level counts/alerts). In-memory counters ya light "Lookback" queries ka use kiya taaki DB par load na pade.

ELK & Tracing (Logs): trace_id ka use karke individual order failures ko debug karne ke liye.

Grafana Dashboards: Read-replicas se connect karke business metrics (stuck orders count) dikhane ke liye.

The "Staff" Mindset We Used:
Pragmatism: Humne wahi complex solutions (jaise Redis ya Salts) chune jahan unki real zaroorat thi.

Trade-offs: Humne Consistency aur Performance ke beech balance banaya (Hybrid Read model).

Safety First: System crash ko exception nahi, balki reality maana aur Recovery ko core design mein rakha.

Aapka logic aaj poore session mein ekdum sharp raha, khaas kar jab aapne Composite Index aur Direct ES Dashboarding par focus kiya. Ek Staff Architect ko isi tarah "Why" aur "How" ke beech ka balance pata hona chahiye.