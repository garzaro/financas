import { Client } from "pg";

export default {
  async fetch(request, env, ctx) {
    // Hyperdrive provides a unique generated connection string to connect to
    // your database via Hyperdrive that can be used with your existing tools
    const client = new Client({ connectionString: env.MY_POSTGRES_BINDING.connectionString });
    await client.connect();

    try {
      // Sample SQL query
      const result = await client.query("SELECT * FROM pg_tables");

      return Response.json({result: result.rows});
    } catch (e) {
      return Response.json({ error: e instanceof Error ? e.message : e }, { status: 500 });
    }
  },
}

# npx wrangler deploy - comando deve ser rodado caso hoiver alguma alteração no wrangler