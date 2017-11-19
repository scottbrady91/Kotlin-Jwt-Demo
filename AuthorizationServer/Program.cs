using System.Collections.Generic;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;
using IdentityServer4.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AuthorizationServer
{
    public class Program
    {
        public static void Main(string[] args)
        {
            WebHost.CreateDefaultBuilder(args)
                    .UseStartup<Startup>()
                    .UseUrls("http://localhost:5000")
                    .Build()
                    .Run();
        }
    }

    public class Startup
    {
        private readonly Client client = new Client
        {
            ClientId = "kotlin_oauth",
            AllowedGrantTypes = GrantTypes.ClientCredentials,
            ClientSecrets = { new Secret("client_password".Sha256()) },
            AllowedScopes = { "api1.read", "api1.write" }
        };

        private ApiResource api = new ApiResource("api1") { Scopes = { new Scope("api1.read"), new Scope("api1.write") } };

        public void ConfigureServices(IServiceCollection services)
        {
            var builder = services.AddIdentityServer()
                .AddInMemoryApiResources(new List<ApiResource> { api })
                .AddInMemoryClients(new List<Client> { client })
                .AddDeveloperSigningCredential();

            services.AddAuthentication()
                .AddIdentityServerAuthentication("Bearer", options =>
                {
                    options.Authority = "http://localhost:5000";
                    options.ApiName = "api1";
                    options.RequireHttpsMetadata = false;
                });
            services.AddMvc();
        }

        public void Configure(IApplicationBuilder app)
        {
            app.UseDeveloperExceptionPage();
            app.UseIdentityServer();
            app.UseAuthentication();
            app.UseMvcWithDefaultRoute();
        }
    }
}