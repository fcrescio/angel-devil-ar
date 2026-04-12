# ADR-0002: Repository Structure

## Status

Accepted.

## Decision

Start with one Android `app` module and explicit package boundaries for `app`, `ui`, `ar`, `tracking`, `character`, `permissions`, and `util`.

## Context

The project needs low ceremony during early AR exploration while still giving agents clear ownership boundaries.

## Consequences

- No premature multi-module Gradle setup.
- Package ownership is documented before feature work starts.
- Future module extraction can happen only after concrete pressure appears.
