import { createClient } from '@insforge/sdk';
import dotenv from 'dotenv';

dotenv.config();

const baseUrl = process.env.INSFORGE_URL || 'https://c2g6m52b.us-east.insforge.app';
const anonKey = process.env.INSFORGE_API_KEY || process.env.INSFORGE_ANON_KEY || '';

export const insforge = createClient({
  baseUrl,
  anonKey
});
